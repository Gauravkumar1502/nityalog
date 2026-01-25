package dev.gaurav.nityalog.entities;

import dev.gaurav.nityalog.enums.TokenType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "token",
        indexes = {
            @Index(name = "idx_token_user_id", columnList = "user_id"),
            @Index(name = "idx_token_hashed_token", columnList = "hashed_token"),
            @Index(name = "idx_token_revoked_at", columnList = "revoked_at"),
            @Index(name = "idx_token_expires_at", columnList = "expires_at"),
            @Index(name = "idx_token_jti", columnList = "jti")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Token extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, length = 30)
    private TokenType tokenType;

    @Column(name = "hashed_token", nullable = false, length = 64)
    private String hashedToken;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revocation_reason", length = 50)
    private String revocationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Token parentToken;

    @Column(name = "jti", unique = true, length = 50)
    private String jti;

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

}