package dev.gaurav.nityalog.entities;

import dev.gaurav.nityalog.enums.OtpType;
import dev.gaurav.nityalog.utils.HashUtils;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.time.Instant;

@Entity
@Table(name = "otp",
        indexes = {
                @Index(name = "idx_otp_user_id", columnList = "user_id"),
                @Index(name = "idx_otp_type", columnList = "otp_type"),
                @Index(name = "idx_otp_expires_at", columnList = "expires_at"),
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Otp extends BaseEntity {

    // Who this OTP belongs to (nullable for pre-registration cases)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", updatable = false)
    private User user;

    @Column(name = "target", length = 100, updatable = false)
    private String target;   // email or phone number

    @Enumerated(EnumType.STRING)
    @Column(name = "otp_type", nullable = false, updatable = false, length = 30)
    private OtpType otpType;

    // Secure storage
    @Column(name = "hashed_otp", nullable = false, updatable = false, length = 128)
    private String otp;

    // Lifecycle
    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    // Security controls
    @Builder.Default
    @Column(name = "attempt_count")
    private int attemptCount = 0;

    @Column(name = "max_attempts", nullable = false, updatable = false)
    private int maxAttempts;

    // Optional: link OTP chain (resend / regenerate)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", updatable = false)
    private Otp parentOtp;

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public void incrementAttemptCount() {
        this.attemptCount++;
    }

    @PrePersist
    private void hashOtp() {
        if (this.otp != null && !this.otp.isBlank()) {
            this.otp = HashUtils.hash(this.otp);
        }
    }

}