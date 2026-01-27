package dev.gaurav.nityalog.dtos;

public record OtpVerificationResponse(
        String message,
        AuthResponse tokenData,  // Only for LOGIN/MFA types for success response
        OtpLimitState verify,
        OtpLimitState resend
) {
    public static OtpVerificationResponse success() {
        return new OtpVerificationResponse("OTP verified successfully", null, null, null);
    }

    public static OtpVerificationResponse withTokens(AuthResponse tokenData) {
        return new OtpVerificationResponse("OTP verified successfully", tokenData, null, null);
    }

    public static OtpVerificationResponse failure(String message, OtpLimitState verify, OtpLimitState resend) {
        return new OtpVerificationResponse(message, null, verify, resend);
    }
}
