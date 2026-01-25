package dev.gaurav.nityalog.exceptions;

import org.springframework.security.oauth2.jwt.BadJwtException;

public class TokenIssuedInFutureException extends BadJwtException {
    public TokenIssuedInFutureException(String message) {
        super(message);
    }
}
