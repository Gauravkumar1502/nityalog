package dev.gaurav.nityalog.exceptions;

import org.springframework.security.oauth2.jwt.BadJwtException;

public class TokenExpiredException extends BadJwtException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
