package dev.gaurav.nityalog.controllers;

import dev.gaurav.nityalog.dtos.*;
import dev.gaurav.nityalog.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class PublicAuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<OtpDispatchResponse>> registerUser(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.registerUser(request)));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<OtpVerificationResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
            return ResponseEntity.ok(ApiResponse.success(authService.verifyOtp(request)));
    }

    @PostMapping("resend-otp")
    public ResponseEntity<ApiResponse<OtpDispatchResponse>> resendRegisterOtp(@Valid @RequestBody ResendOtpRequest request) {
            return ResponseEntity.ok(ApiResponse.success(authService.resendOtp(request)));
    }

}
