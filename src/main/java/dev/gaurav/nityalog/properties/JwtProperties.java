package dev.gaurav.nityalog.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotNull(message = "Issuer must not be null")
        URI issuer,

        @NotEmpty(message = "Audience must not be empty")
        String audience,

        @NotNull(message = "Access TTL must not be null")
        @DurationUnit(ChronoUnit.MINUTES)
        Duration accessTtl,

        @NotNull(message = "Refresh TTL must not be null")
        @DurationUnit(ChronoUnit.DAYS)
        Duration refreshTtl,

        String keyRotationCron
) {
        public JwtProperties {
                // Validate TTL constraints
                if (accessTtl.compareTo(Duration.ofMinutes(5)) < 0) {
                        throw new IllegalArgumentException("Access TTL must be at least 5 minutes");
                }
                if (accessTtl.compareTo(Duration.ofHours(2)) > 0) {
                        throw new IllegalArgumentException("Access TTL must not exceed 2 hours");
                }
                if (refreshTtl.compareTo(Duration.ofDays(1)) < 0) {
                        throw new IllegalArgumentException("Refresh TTL must be at least 1 day");
                }
                if (refreshTtl.compareTo(Duration.ofDays(30)) > 0) {
                        throw new IllegalArgumentException("Refresh TTL must not exceed 30 days");
                }
        }
}
