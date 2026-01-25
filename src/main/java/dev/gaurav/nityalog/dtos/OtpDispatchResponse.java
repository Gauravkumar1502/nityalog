package dev.gaurav.nityalog.dtos;

import lombok.Builder;

import java.time.Instant;

@Builder
public record OtpDispatchResponse(
        String message,
        OtpLimitState verify,
        OtpLimitState resend
) {
}
