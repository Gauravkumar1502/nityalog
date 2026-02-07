package dev.gaurav.nityalog.configs;

import dev.gaurav.nityalog.properties.CorsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final CorsProperties corsProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(corsProperties.allowedOrigins());
        cors.setAllowedMethods(corsProperties.allowedMethods());
        cors.setAllowedHeaders(corsProperties.allowedHeaders());
        // Only expose headers if configured
        List<String> exposedHeaders = corsProperties.exposedHeaders();
        if (exposedHeaders != null && !exposedHeaders.isEmpty()) {
            cors.setExposedHeaders(exposedHeaders);
        }
        cors.setAllowCredentials(corsProperties.allowCredentials());
        cors.setMaxAge(corsProperties.maxAge());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }}
