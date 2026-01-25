package dev.gaurav.nityalog.models;


import dev.gaurav.nityalog.enums.TokenType;
import java.time.Duration;

public record TokenData(TokenType tokenType, String token, String jti, Duration expiresIn) {
}