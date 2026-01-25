package dev.gaurav.nityalog.services;

import dev.gaurav.nityalog.dtos.OtpLimitState;
import dev.gaurav.nityalog.dtos.OtpVerificationResponse;
import dev.gaurav.nityalog.entities.Otp;
import dev.gaurav.nityalog.entities.User;
import dev.gaurav.nityalog.enums.OtpType;
import dev.gaurav.nityalog.enums.TokenType;
import dev.gaurav.nityalog.exceptions.InvalidOtpException;
import dev.gaurav.nityalog.exceptions.LimitExceededException;
import dev.gaurav.nityalog.exceptions.TooManyRequestsException;
import dev.gaurav.nityalog.models.TokenData;
import dev.gaurav.nityalog.repositories.OtpRepository;
import dev.gaurav.nityalog.utils.HashUtils;
import dev.gaurav.nityalog.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OtpService {
    private final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final OtpRepository otpRepository;
    private final SecureRandom RANDOM = new SecureRandom();

    public String generateOtpCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(ALPHANUMERIC.length());
            sb.append(ALPHANUMERIC.charAt(index));
        }
        return sb.toString();
    }

    public String generateOtpCode() {
        return generateOtpCode(8);
    }

    public Otp save(Otp otp) {
        return otpRepository.save(otp);
    }

    public long countRecentOtps(String target, OtpType otpType, Duration duration) {
        return otpRepository.countByTargetAndOtpTypeAndCreatedAtAfter(target, otpType, Instant.now().minus(duration));
    }

    public Optional<Otp> getLatestOtp(String target, OtpType otpType) {
        return otpRepository.findTopByTargetAndOtpTypeOrderByCreatedAtDesc(target, otpType);
    }

    public Optional<Otp> getLatestOtp(User user, OtpType otpType) {
        return otpRepository.findTopByUserAndOtpTypeOrderByCreatedAtDesc(user, otpType);
    }

    public Optional<Instant> getLastSuccessfulOtpUseTime(String target, OtpType otpType) {
        return otpRepository.findLastSuccessfulUsedAt(target, otpType);
    }

    public long getFailedAttemptsCount(String target, OtpType otpType, Duration from) {
        return otpRepository.countFailedAttempts(target, otpType, Instant.now().minus(from));
    }

    public OtpLimitState validateOtpResend(String target, OtpType otpType) {
        return validateOtpResend(target, null, otpType);
    }

    public OtpLimitState validateOtpResend(User user, OtpType otpType) {
        return validateOtpResend(null, user, otpType);
    }

    public OtpLimitState validateOtpResend(String target, User user, OtpType otpType) {
        ValidationUtils.assertExactlyOneNotNull(target, user, "Either identifier or user must be provided, but not both.");

        Instant windowStart = Instant.now().minus(otpType.getRateLimitWindow());
        Optional<Instant> lastSuccess = (user != null)
            ? otpRepository.findLastSuccessfulUsedAt(user, otpType)
            : otpRepository.findLastSuccessfulUsedAt(target, otpType);

        if (lastSuccess.isPresent() && lastSuccess.get().isAfter(windowStart)) {
            windowStart = lastSuccess.get();
        }

        long used = (user != null)
            ? otpRepository.countByUserAndOtpTypeAndCreatedAtAfter(user, otpType, windowStart)
            : otpRepository.countByTargetAndOtpTypeAndCreatedAtAfter(target, otpType, windowStart);

        return buildLimitState(
                used,
                otpType.getMaxRequestsPerWindow(),
                windowStart, otpType.getRateLimitWindow(),
            "OTP resend limit reached."
        );
    }

    public OtpLimitState validateFailedAttempts(String target, OtpType otpType) {
        return validateFailedAttempts(target, null, otpType);
    }

    public OtpLimitState validateFailedAttempts(User user, OtpType otpType) {
        return validateFailedAttempts(null, user, otpType);
    }

    public OtpLimitState validateFailedAttempts(String target, User user, OtpType otpType) {
        ValidationUtils.assertExactlyOneNotNull(target, user, "Either identifier or user must be provided, but not both.");

        Instant windowStart = Instant.now().minus(otpType.getVerificationAttemptWindow());
        Optional<Instant> lastSuccess = (user != null)
            ? otpRepository.findLastSuccessfulUsedAt(user, otpType)
            : otpRepository.findLastSuccessfulUsedAt(target, otpType);

        if (lastSuccess.isPresent() && lastSuccess.get().isAfter(windowStart)) {
            windowStart = lastSuccess.get();
        }

        long failed = (user != null)
            ? otpRepository.countFailedAttempts(user, otpType, windowStart)
            : otpRepository.countFailedAttempts(target, otpType, windowStart);

        return buildLimitState(
                failed,
                otpType.getMaxVerificationAttempts(),
                windowStart,
                otpType.getVerificationAttemptWindow(),
                "OTP verification attempt limit reached."
        );
    }

    private OtpLimitState buildLimitState(long used, long max, Instant windowStart, Duration window) {
        return buildLimitState(used, max, windowStart, window, null);
    }

    private OtpLimitState buildLimitState(long used, long max, Instant windowStart, Duration window, String message) {
        long left = Math.max(max - used, 0);
        Instant resetAt = windowStart.plus(window);
        String finalMessage = message == null ? "Limit reached." : message;
        if (left == 0) {
            throw new TooManyRequestsException(finalMessage + " Try again after " + resetAt);
        }
        return new OtpLimitState(left, resetAt);
    }

    public OtpVerificationResponse verifyOtp(String otpCode, String target, User user, OtpType otpType) {
        ValidationUtils.assertExactlyOneNotNull(target, user, "Either identifier or user must be provided, but not both.");

        long attemptLeft = validateFailedAttempts(target, user, otpType).attemptsLeft();
        if (attemptLeft == 0) {
            throw new LimitExceededException("Maximum OTP verification attempts exceeded.");
        }
        Optional<Otp> otpOptional = (user != null)
            ? otpRepository.findTopByUserAndOtpTypeOrderByCreatedAtDesc(user, otpType)
            : otpRepository.findTopByTargetAndOtpTypeOrderByCreatedAtDesc(target, otpType);

        if (otpOptional.isEmpty()) {
            throw new InvalidOtpException("No OTP found for the given identifier and type.");
        }

        Otp otp = otpOptional.get();

        if (otp.isUsed() || otp.isRevoked() || otp.isExpired()) {
            log.warn("Invalid OTP state - used: {}, revoked: {}, expired: {} for type: {}",
                 otp.isUsed(), otp.isRevoked(), otp.isExpired(), otpType);
            throw new InvalidOtpException("The OTP is no longer valid.");
        }

        if (otp.getAttemptCount() >= otp.getMaxAttempts()) {
            throw new LimitExceededException("Maximum OTP verification attempts exceeded.");
        }

        if (!HashUtils.verify(otpCode, otp.getOtp())) {
            otp.incrementAttemptCount();
            otpRepository.save(otp);
            throw new InvalidOtpException("Invalid OTP code.");
        }

        otp.setUsedAt(Instant.now());
        otpRepository.save(otp);
        log.info("OTP verified successfully for identifier::type: {}/{}", target != null ? target : user.getId(), otpType);
        return switch (otpType) {
            case LOGIN, MFA, EMAIL_VERIFY, PHONE_VERIFY -> {
                // TODO:: Generate token data
                TokenData tokenData = new TokenData(TokenType.ACCESS, "token", "jti", Duration.ofHours(1));
                yield OtpVerificationResponse.withToken(tokenData);
            }
            case RESET_PASSWORD -> OtpVerificationResponse.success();
        };
    }
}
