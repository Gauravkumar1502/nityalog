package dev.gaurav.nityalog.entities;

import dev.gaurav.nityalog.enums.ProviderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "user_provider",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "provider_type"}),
        indexes = {
            @Index(name = "idx_user_provider", columnList = "user_id, provider_type")
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

    @Column(name = "provider_user_id")
    private String providerUserId;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;
}