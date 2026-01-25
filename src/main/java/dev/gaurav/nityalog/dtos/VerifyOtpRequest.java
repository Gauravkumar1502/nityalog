package dev.gaurav.nityalog.dtos;

import dev.gaurav.nityalog.enums.OtpType;

public record VerifyOtpRequest(
        String identifier,
        String otp,
        OtpType otpType
) {}

