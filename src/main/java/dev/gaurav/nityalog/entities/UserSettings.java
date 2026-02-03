package dev.gaurav.nityalog.entities;

import dev.gaurav.nityalog.converters.LocaleConverter;
import dev.gaurav.nityalog.enums.Theme;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.ZoneId;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "user_settings",
       uniqueConstraints = {
           @UniqueConstraint(name = "uc_user_settings_user", columnNames = "user_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserSettings extends AuditableEntity {

    @Id
    @Column(name = "user_id")
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "theme", length = 20, nullable = false)
    private Theme theme = Theme.SYSTEM;

    @Convert(converter = LocaleConverter.class)
    @Column(name = "locale", length = 10, nullable = false)
    private Locale locale = Locale.forLanguageTag("en-IN");

    @Column(name = "timezone", length = 50, nullable = false)
    private ZoneId timezone = ZoneId.of("UTC");

    @Builder.Default
    @Column(name = "email_notifications")
    private boolean emailNotifications = false;

    @Builder.Default
    @Column(name = "push_notifications")
    private boolean pushNotifications = false;
}
