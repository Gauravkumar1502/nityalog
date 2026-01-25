package dev.gaurav.nityalog.dtos;

import dev.gaurav.nityalog.models.TokenData;

public record OtpVerificationResponse(
        String message,
        TokenData tokenData,  // Only for LOGIN/MFA types for success response
        OtpLimitState verify,
        OtpLimitState resend
) {
    public static OtpVerificationResponse success() {
        return new OtpVerificationResponse("OTP verified successfully", null, null, null);
    }

    public static OtpVerificationResponse withToken(TokenData tokenData) {
        return new OtpVerificationResponse("OTP verified successfully", tokenData, null, null);
    }

    public static OtpVerificationResponse failure(String message, OtpLimitState verify, OtpLimitState resend) {
        return new OtpVerificationResponse(message, null, verify, resend);
    }
}
