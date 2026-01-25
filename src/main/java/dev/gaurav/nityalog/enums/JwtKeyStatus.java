package dev.gaurav.nityalog.enums;

public enum JwtKeyStatus {
    ACTIVE,   // Used for signing
    RETIRED,  // No longer used for signing, but can verify old tokens
    REVOKED,  // Compromised / revoked
    EXPIRED   // Completely invalid
}