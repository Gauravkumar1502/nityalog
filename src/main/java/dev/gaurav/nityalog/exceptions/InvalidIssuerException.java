package dev.gaurav.nityalog.exceptions;

import org.springframework.security.oauth2.jwt.BadJwtException;

public class InvalidIssuerException extends BadJwtException {
    public InvalidIssuerException(String message) {
        super(message);
    }
}
