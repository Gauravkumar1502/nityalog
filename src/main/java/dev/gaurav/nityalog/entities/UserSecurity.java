package dev.gaurav.nityalog.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.time.Instant;

@Entity
@Table(name = "user_security",
        uniqueConstraints = {
            @UniqueConstraint(name = "uc_user_security_user", columnNames = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserSecurity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "last_login_at")
    @Convert(converter = Jsr310JpaConverters.InstantConverter.class)
    private Instant lastLogin;

    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false)
    @Convert(converter = Jsr310JpaConverters.InstantConverter.class)
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    @Convert(converter = Jsr310JpaConverters.InstantConverter.class)
    private Instant lockedUntil;

    @Column(name = "last_password_change_at")
    @Convert(converter = Jsr310JpaConverters.InstantConverter.class)
    private Instant lastPasswordChangeAt;

    @Builder.Default
    @Column(name = "mfa_enabled")
    private boolean mfaEnabled = false;

    public void increaseFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }
}