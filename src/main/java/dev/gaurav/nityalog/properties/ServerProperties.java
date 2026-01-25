package dev.gaurav.nityalog.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "server")
public record ServerProperties(
        @NotBlank(message = "Server scheme must not be blank")
        String scheme,

        @NotBlank(message = "Server address must not be blank")
        String address,

        @NotNull(message = "Server port must not be null")
        @Min(value = 1, message = "Server port must be >= 1")
        @Max(value = 65535, message = "Server port must be <= 65535")
        Integer port
) {
    public String getBaseUrl() {
        return scheme + "://" + address + ":" + port;
    }
}
