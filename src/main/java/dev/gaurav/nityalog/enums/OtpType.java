package dev.gaurav.nityalog.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public enum OtpType {
    LOGIN(5,
            Duration.ofMinutes(10),
            5,
            Duration.ofMinutes(15),
            Duration.ofMinutes(5)
    ),

    EMAIL_VERIFY(5,
            Duration.ofMinutes(15),
            5,
            Duration.ofHours(1),
            Duration.ofMinutes(10)
    ),

    RESET_PASSWORD(3,
            Duration.ofMinutes(30),
            3,
            Duration.ofMinutes(30),
            Duration.ofMinutes(10)
    ),

    PHONE_VERIFY(5,
            Duration.ofMinutes(15),
            3,
            Duration.ofMinutes(30),
            Duration.ofMinutes(5)
    ),

    MFA(3,
            Duration.ofMinutes(10),
            3,
            Duration.ofMinutes(15),
            Duration.ofMinutes(3)
    );

    private final int maxVerificationAttempts;
    private final Duration verificationAttemptWindow;

    private final int maxRequestsPerWindow;
    private final Duration rateLimitWindow;

    private final Duration expiresIn;
}

