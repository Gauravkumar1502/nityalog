package dev.gaurav.nityalog.constants;

public class SecurityConstants {
    public static final String[] PUBLIC_PATHS = {
            "/.well-known/jwks.json",

            "/api/v1/public/**",

            // Public Auth Endpoints
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/verify",
            "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",

            "/actuator/**",

            "/error",
            "/webjars/**",
            "/assets/**"
    };
}
