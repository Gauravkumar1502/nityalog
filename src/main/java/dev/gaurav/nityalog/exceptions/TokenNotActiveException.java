package dev.gaurav.nityalog.exceptions;

import org.springframework.security.oauth2.jwt.BadJwtException;

public class TokenNotActiveException extends BadJwtException {
    public TokenNotActiveException(String message) {
        super(message);
    }
}
