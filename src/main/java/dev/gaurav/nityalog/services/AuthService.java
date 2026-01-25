package dev.gaurav.nityalog.services;

import dev.gaurav.nityalog.dtos.*;
import dev.gaurav.nityalog.entities.Otp;
import dev.gaurav.nityalog.entities.User;
import dev.gaurav.nityalog.enums.OtpType;
import dev.gaurav.nityalog.exceptions.EmailAlreadyExistsException;
import dev.gaurav.nityalog.exceptions.UserNotFoundException;
import dev.gaurav.nityalog.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final OtpService otpService;
    private final MailService mailService;
    private final UserService userService;

    public OtpDispatchResponse registerUser(RegisterRequest request) {
        String email = request.email();

        if (userService.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email is already registered.");
        }
        return sendOtp(email, null, OtpType.EMAIL_VERIFY);
    }

    public OtpDispatchResponse resendOtp(ResendOtpRequest request) {
        String identifier = request.identifier();
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
                    throw new EmailAlreadyExistsException("Email is already registered.");
                }
            }
            case LOGIN, RESET_PASSWORD, MFA -> {
                if (user == null) {
                    throw new UserNotFoundException("User must be provided for this OTP type.");
                }
            }
        }

        OtpLimitState resendState = otpService.validateOtpResend(target, user, otpType);
        OtpLimitState verifyState = otpService.validateFailedAttempts(target, user, otpType);

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
                .resend(resendState)
                .verify(verifyState)
                .build();
    }

    public OtpVerificationResponse verifyOtp(VerifyOtpRequest request) {
        String identifier = request.identifier();
        String otpCode = request.otp();
        OtpType otpType = request.otpType();

        User user = null;
        String target = null;

        switch (otpType) {
            case EMAIL_VERIFY, PHONE_VERIFY -> {
                target = identifier;
            }
            case LOGIN, RESET_PASSWORD, MFA -> {
                user = userService.loadUserByUsername(identifier);
            }
        }

        return otpService.verifyOtp(otpCode, target, user, otpType);
    }

}
