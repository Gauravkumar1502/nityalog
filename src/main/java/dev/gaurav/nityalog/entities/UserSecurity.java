package dev.gaurav.nityalog.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.time.Instant;
import java.util.UUID;

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
public class UserSecurity extends AuditableEntity {

    @Id
    @Column(name = "user_id")
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "last_login_at")
    private Instant lastLogin;

    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_password_change_at")
    private Instant lastPasswordChangeAt;

    @Builder.Default
    @Column(name = "mfa_enabled")
    private boolean mfaEnabled = false;

    public void increaseFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }
}