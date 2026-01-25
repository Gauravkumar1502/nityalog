package dev.gaurav.nityalog.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties (
        @NotEmpty(message = "At least one allowed origin must be configured")
        List<String> allowedOrigins,

        @NotEmpty(message = "At least one allowed method must be configured")
        List<String> allowedMethods,

        @NotEmpty(message = "At least one allowed header must be configured")
        List<String> allowedHeaders,

        List<String> exposedHeaders,

        @NotNull(message = "allowCredentials must not be null")
        Boolean allowCredentials,

        @NotNull(message = "CORS maxAge must not be null")
        @DurationUnit(ChronoUnit.SECONDS)
        Duration maxAge
) {
        public CorsProperties {
                // Validate maxAge constraints
                if (maxAge.compareTo(Duration.ZERO) < 0) {
                    throw new IllegalArgumentException("CORS maxAge must be >= 0");
                }
                if (maxAge.compareTo(Duration.ofDays(1)) > 0) {
                    throw new IllegalArgumentException("CORS maxAge must not exceed 1 day");
                }
        }
}
