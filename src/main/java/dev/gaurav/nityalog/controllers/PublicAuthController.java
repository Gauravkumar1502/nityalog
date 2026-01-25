package dev.gaurav.nityalog.controllers;

import dev.gaurav.nityalog.dtos.ApiError;
import dev.gaurav.nityalog.dtos.ApiResponse;
import dev.gaurav.nityalog.dtos.RegisterRequest;
import dev.gaurav.nityalog.dtos.ResendOtpRequest;
import dev.gaurav.nityalog.entities.Otp;
import dev.gaurav.nityalog.enums.OtpType;
import dev.gaurav.nityalog.services.AuthService;
import dev.gaurav.nityalog.services.MailService;
import dev.gaurav.nityalog.services.OtpService;
import dev.gaurav.nityalog.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class PublicAuthController {
    private final AuthService authService;
    private final OtpService otpService;
    private final MailService mailService;
    private final UserService userService;


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody RegisterRequest request) {
        if(userService.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse
                    .error(400, ApiError.badRequest("Email is already registered.", "/api/v1/auth/register")));
        }
        String generateOtp = otpService.generateOtpCode();
        Otp otp = Otp.builder()
                .otpType(OtpType.EMAIL_VERIFY)
                .target(request.email())
                .otp(generateOtp)
                .expiresAt(Instant.now().plus(OtpType.EMAIL_VERIFY.getExpiresIn()))
                .maxAttempts(5)
                .build();
        otpService.save(otp);
        mailService.sendOtpEmail(request.email(), otp);
        return ResponseEntity.ok(ApiResponse.success("Registration OTP sent to email."));
    }

    // resend register otp
    @PostMapping("resend-otp")
    public ResponseEntity<ApiResponse<String>> resendRegisterOtp(@Valid @RequestBody ResendOtpRequest request, HttpServletRequest httpRequest) {
        String target = request.target();
        OtpType otpType = request.otpType();
        switch (otpType) {
            case EMAIL_VERIFY, PHONE_VERIFY -> {
                if(userService.existsByEmail(target)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse
                            .error(400, ApiError.badRequest("Email is already registered.", httpRequest.getRequestURI())));
                }
            }
            case LOGIN, FORGOT_PASSWORD -> {
                if(!userService.existsByEmail(target)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse
                            .error(400, ApiError.badRequest("Email is not registered.", httpRequest.getRequestURI())));
                }
            }
            case MFA -> {}
        }
        otpService.getLastSuccessfulOtpUseTime(target, otpType)
        long recentOtpCount = otpService.countRecentOtps(target, otpType, otpType.getRateLimitWindow());
        if (recentOtpCount >= otpType.getMaxRequestsPerWindow()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiResponse
                    .error(429, ApiError.tooManyRequests("OTP resend limit reached. Please try later.", httpRequest.getRequestURI())));
        }
        Optional<Otp> latestOtp = otpService.getLatestOtp(target, otpType);

        String newOtp = otpService.generateOtpCode();
        Otp otp = Otp.builder()
                .otpType(otpType)
                .target(target)
                .otp(newOtp)
                .expiresAt(Instant.now().plus(otpType.getExpiresIn()))
                .maxAttempts(otpType.getMaxVerificationAttempts())
                .build();
        otpService.save(otp);
        mailService.sendOtpEmail(target, otp);
        return ResponseEntity.ok(ApiResponse.success("Registration OTP resent to email."));
    }

}
