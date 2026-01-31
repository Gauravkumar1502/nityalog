package dev.gaurav.nityalog.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@RequiredArgsConstructor
@Slf4j
public enum ProviderType {
    EMAIL("Email", 0),
    GOOGLE("Google", 1),
    GITHUB("GitHub", 2),
    FACEBOOK("Facebook", 3),
    TWITTER("Twitter", 4),
    DISCORD("Discord", 5),
    LINKEDIN("Linkedin", 6);

    private final String displayName;
    private final int code;

    public static ProviderType fromCode(int code) {
        for (ProviderType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown provider code: " + code);
    }

     public ProviderType getProviderTypeFromRegistrationId(String registrationId) {
        return switch (registrationId) {
            case "email" -> EMAIL;
            case "google" -> GOOGLE;
            case "github" -> GITHUB;
            case "facebook" -> FACEBOOK;
            case "twitter" -> TWITTER;
            case "discord" -> DISCORD;
            case "linkedin" -> LINKEDIN;
            default -> {
                log.error("Unsupported OAuth2 provider: {}", registrationId);
                throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
            }
        };
    }
}
