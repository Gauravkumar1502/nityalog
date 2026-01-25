package dev.gaurav.nityalog.dtos;

import dev.gaurav.nityalog.models.TokenData;

public record OtpVerificationResponse(
        String message,
        TokenData tokenData  // Only for LOGIN/MFA types
) {
    public static OtpVerificationResponse success() {
        return new OtpVerificationResponse("OTP verified successfully", null);
    }

    public static OtpVerificationResponse withToken(TokenData tokenData) {
        return new OtpVerificationResponse("OTP verified successfully", tokenData);
    }
}
