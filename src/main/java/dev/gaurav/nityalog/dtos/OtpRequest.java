package dev.gaurav.nityalog.dtos;

import dev.gaurav.nityalog.enums.OtpType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OtpRequest(
        @NotBlank String identifier,
        @NotNull OtpType otpType
) {
}
