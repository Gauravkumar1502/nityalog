package dev.gaurav.nityalog.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "user_metadata",
        uniqueConstraints = {
            @UniqueConstraint(name = "uc_user_metadata_user", columnNames = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserMetadata extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "FK_USER_METADATA_ON_USER"))
    private User user;

    private Instant verifiedAt;

    @Column(name = "last_login")
    private Instant lastLogin;

    @Builder.Default
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_password_change")
    private Instant lastPasswordChangeAt;

    public void increaseFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }
}