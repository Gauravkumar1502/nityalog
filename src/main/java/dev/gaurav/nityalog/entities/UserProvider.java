package dev.gaurav.nityalog.entities;

import dev.gaurav.nityalog.enums.ProviderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.time.Instant;

@Entity
@Table(name = "user_provider",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_provider__user_provider",
                        columnNames = {"user_id", "provider_type"}
                ),
                @UniqueConstraint(
                        name = "uk_user_provider__provider_type_provider_user_id",
                        columnNames = {"provider_type", "provider_user_id"}
                )},
        indexes = {
                @Index(name = "idx_user_provider_user", columnList = "user_id"),
                @Index(name = "idx_user_provider__provider_type_provider_user_id", columnList = "provider_type, provider_user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserProvider extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "provider_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(name = "provider_username")
    private String providerUsername;

    @Column(name = "provider_email")
    private String providerEmail;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;
}