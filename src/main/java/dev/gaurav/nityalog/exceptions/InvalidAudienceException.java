package dev.gaurav.nityalog.exceptions;

import org.springframework.security.oauth2.jwt.BadJwtException;

public class InvalidAudienceException extends BadJwtException {
    public InvalidAudienceException(String message) {
        super(message);
    }
}
