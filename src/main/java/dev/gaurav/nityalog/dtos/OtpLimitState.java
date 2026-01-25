package dev.gaurav.nityalog.dtos;

import java.time.Instant;

public record OtpLimitState(long attemptsLeft, Instant resetAt) {
}
