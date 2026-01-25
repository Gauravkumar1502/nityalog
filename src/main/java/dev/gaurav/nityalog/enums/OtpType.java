package dev.gaurav.nityalog.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public enum OtpType {
    LOGIN(3, Duration.ofMinutes(5), 5, Duration.ofMinutes(30)),
    EMAIL_VERIFY(5, Duration.ofMinutes(10), 5, Duration.ofHours(1)),
    FORGOT_PASSWORD(3, Duration.ofMinutes(30), 5, Duration.ofMinutes(30)),
    PHONE_VERIFY(5, Duration.ofMinutes(10), 5, Duration.ofHours(1)),
    MFA(5, Duration.ofMinutes(5), 5, Duration.ofMinutes(30));

    private final int maxVerificationAttempts;
    private final Duration expiresIn;

    private final int maxRequestsPerWindow;
    private final Duration rateLimitWindow;
}

