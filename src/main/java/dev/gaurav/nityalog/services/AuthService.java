package dev.gaurav.nityalog.services;

import dev.gaurav.nityalog.dtos.*;
import dev.gaurav.nityalog.entities.Otp;
import dev.gaurav.nityalog.entities.User;
import dev.gaurav.nityalog.entities.UserProvider;
import dev.gaurav.nityalog.enums.OtpType;
import dev.gaurav.nityalog.enums.ProviderType;
import dev.gaurav.nityalog.exceptions.EmailAlreadyExistsException;
import dev.gaurav.nityalog.exceptions.LimitExceededException;
import dev.gaurav.nityalog.exceptions.UserNotFoundException;
import dev.gaurav.nityalog.models.TokenData;
import dev.gaurav.nityalog.security.jwt.JwtService;
import dev.gaurav.nityalog.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final OtpService otpService;
    private final MailService mailService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final UserProviderService userProviderService;

    public OtpDispatchResponse registerUser(RegisterRequest request) {
        String email = request.email().trim();

        if (userService.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("The provided email address is already associated with an existing account.");
        }
        return sendOtp(email, null, OtpType.EMAIL_VERIFY);
    }

    public OtpDispatchResponse resendOtp(OtpRequest request) {
        String identifier = request.identifier().trim();
        OtpType otpType = request.otpType();

        return switch (otpType) {
            case LOGIN, RESET_PASSWORD, MFA -> {
//              TODO:: validate that identifier is a username or email
                User user = userService.loadUserByUsername(identifier);
                yield sendOtp(null, user, otpType);
            }
            case EMAIL_VERIFY, PHONE_VERIFY -> {
//              TODO:: validate that identifier is an email or phone number
                yield sendOtp(identifier, null, otpType);
            }
        };
    }

    // For pre-registration (user not yet created)
    public OtpDispatchResponse sendOtp(String target, OtpType otpType) {
        return sendOtp(target, null, otpType);
    }

    // For registered users
    public OtpDispatchResponse sendOtp(User user, OtpType otpType) {
        return sendOtp(null, user, otpType);
    }

    private OtpDispatchResponse sendOtp(String target, User user, OtpType otpType) {
        ValidationUtils
                .assertExactlyOneNotNull(target, user, "Either identifier or user must be provided, but not both.");

        switch (otpType) {
            case EMAIL_VERIFY, PHONE_VERIFY -> {
                if (user != null) {
                    throw new IllegalArgumentException("EMAIL_VERIFY/PHONE_VERIFY OTP must use identifier, not user.");
                }
                if (userService.existsByEmail(target)) {
                    throw new EmailAlreadyExistsException("The provided email address is already associated with an existing account.");
                }
            }
            case LOGIN, RESET_PASSWORD, MFA -> {
                if (user == null) {
                    throw new UserNotFoundException("User must be provided for this OTP type.");
                }
            }
        }

        OtpLimitState resendState = otpService.getOtpResendState(target, user, otpType);
        if (resendState.attemptsLeft() == 0) {
            throw new LimitExceededException("Maximum OTP resends exceeded.");
        }

        Optional<Otp> lastOtpOptional = (user != null)
            ? otpService.getLatestOtp(user, otpType)
            : otpService.getLatestOtp(target, otpType);

        Otp parentOtp = null;
        if (lastOtpOptional.isPresent()) {
            Otp lastOtp = lastOtpOptional.get();
            if (!lastOtp.isUsed() && !lastOtp.isRevoked() && !lastOtp.isExpired()) {
                lastOtp.setRevokedAt(Instant.now());
                otpService.save(lastOtp);
            }
            parentOtp = lastOtp;
        }

        String rawOtp = otpService.generateOtpCode();
        log.info("Generated OTP for {}: {}", target, rawOtp);
        Otp otp = Otp.builder()
                .otpType(otpType)
                .user(user)
                .target(target)
                .otp(rawOtp)
                .expiresAt(Instant.now().plus(otpType.getExpiresIn()))
                .maxAttempts(otpType.getMaxVerificationAttempts())
                .attemptCount(0)
                .parentOtp(parentOtp)
                .build();
        otpService.save(otp);

        mailService.sendOtpEmail(
                user != null ? user.getEmail() : target,
                rawOtp,
                otpType
        );

        return OtpDispatchResponse.builder()
                .message("OTP sent successfully.")
                .resend(otpService.getOtpResendState(target, user, otpType))
                .verify(otpService.getFailedAttemptsState(target, user, otpType))
                .build();
    }

    public OtpVerificationResponse verifyOtp(VerifyOtpRequest request) {
        String identifier = request.identifier().trim();
        String otpCode = request.otp().trim();
        OtpType otpType = request.otpType();

        return switch (otpType) {
            case EMAIL_VERIFY, PHONE_VERIFY ->
                    otpService.verifyOtp(otpCode, identifier, null, otpType);
            case LOGIN, RESET_PASSWORD, MFA ->
                    otpService.verifyOtp(otpCode,null, userService.loadUserByUsername(identifier), otpType);
        };
    }

    public AuthResponse loginUser(LoginRequest request) {
        String usernameOrEmail = request.usernameOrEmail().trim();
        String rawPassword = request.password().trim();
        User user = userService.loadUserByUsername(usernameOrEmail);
        String password = user.getPassword();
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("No password set for this user. Please login using OTP and set a password.");
        }

        if (!passwordEncoder.matches(rawPassword, password)) {
            throw new BadCredentialsException("Invalid username/email or password.");
        }

        userService.validateUserStatus(user);
        return issueTokens(user);
    }

    public AuthResponse issueTokens(User user) {
        TokenData accessTokenData = jwtService.generateAccessToken(user);
        TokenData refreshTokenData = jwtService.generateRefreshToken(user);
        tokenService.save(user, List.of(accessTokenData, refreshTokenData));

        return AuthResponse.builder()
                .accessToken(accessTokenData.token())
                .refreshToken(refreshTokenData.token())
                .expiresIn(accessTokenData.expiresIn().toSeconds())
                .refreshExpiresIn(refreshTokenData.expiresIn().toSeconds())
                .tokenType("Bearer")
                .build();
    }

    public User processOAuth2User(OAuth2User oauthUser, ProviderType provider) {
        String providerUserId = oauthUser.getAttribute(provider.getProviderUserIdKey());
        if (providerUserId == null || providerUserId.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    "missing_provider_user_id",
                    "OAuth2 login failed: missing provider user id for " + provider,
                    null));
        }

        Optional<UserProvider> existingProvider = userProviderService.findByProviderAndProviderUserId(provider, providerUserId);
        if (existingProvider.isPresent()) {
            User user = existingProvider.get().getUser();
            existingProvider.get().setLastUsedAt(Instant.now());
            user.getSecurity().setLastLogin(Instant.now());
            return userService.save(user);
        }
        return createAndLinkOAuthUser(oauthUser, provider, providerUserId);
    }

    private User createAndLinkOAuthUser(OAuth2User oauthUser, ProviderType provider, String providerUserId) {
       User user =  switch (provider) {
           case GOOGLE -> {
                String email = oauthUser.getAttribute("email");
                String name = oauthUser.getAttribute("name");
                String givenName = oauthUser.getAttribute("given_name");
                String familyName = oauthUser.getAttribute("family_name");
                String picture = oauthUser.getAttribute("picture");

                if (email == null || email.isBlank()) {
                    throw new OAuth2AuthenticationException(new OAuth2Error(
                            "missing_email",
                            "Google OAuth2 response missing email",
                            null));
                }

                User existingUser = userService.findByEmail(email).orElse(null);

                if (existingUser != null) {
                    yield existingUser;
                }

                String username = userService.generateUsernameFromEmail(email);
                User newUser = userService.createOAuthUser(username, email);

                newUser.addProvider(UserProvider.builder()
                        .user(newUser)
                        .providerType(ProviderType.EMAIL)
                        .providerUsername(username)
                        .providerUserId(providerUserId)
                        .providerEmail(email)
                        .build());

                newUser.getProfile().setFirstName(givenName);
                newUser.getProfile().setLastName(familyName);
                newUser.getProfile().setAvatarUrl(picture);
                yield userService.save(newUser);
            }
            case EMAIL, PHONE -> throw new OAuth2AuthenticationException(new OAuth2Error(
                    "unsupported_provider",
                    "Unsupported OAuth2 provider: " + provider,
                    null
            ));
        };

        UserProvider userProvider = UserProvider.builder()
                .user(user)
                .providerType(provider)
                .providerUserId(providerUserId)
                .providerUsername(user.getUsername())
                .providerEmail(user.getEmail())
                .build();
        userProvider.setLastUsedAt(Instant.now());
        user.addProvider(userProvider);
        user.getSecurity().setLastLogin(Instant.now());
        return userService.save(user);
    }

}
