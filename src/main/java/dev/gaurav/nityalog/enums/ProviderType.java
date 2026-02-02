package dev.gaurav.nityalog.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@RequiredArgsConstructor
@Slf4j
public enum ProviderType {
    EMAIL("email", "Email", 0),
    GOOGLE("google", "Google", 1);
    // For Future Use
//    GITHUB("github", "GitHub", 2),
//    FACEBOOK("facebook", "Facebook", 3),
//    TWITTER("twitter", "Twitter", 4),
//    DISCORD("discord", "Discord", 5),
//    LINKEDIN("linkedin", "LinkedIn", 6);

    private final String registrationId;
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

    public static ProviderType fromOauth2RegistrationId(String registrationId) {
        if (registrationId == null) {
            throw new IllegalArgumentException("OAuth2 registrationId cannot be null");
        }

        for (ProviderType type : values()) {
            if (type.getRegistrationId().equalsIgnoreCase(registrationId)) {
                return type;
            }
        }

        log.error("Unsupported OAuth2 provider: {}", registrationId);
        throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
    }

}
