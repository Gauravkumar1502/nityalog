package dev.gaurav.nityalog.dtos;

import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,
        long expiresIn,
        String refreshToken,
        long refreshExpiresIn,
        String tokenType
) {
}
