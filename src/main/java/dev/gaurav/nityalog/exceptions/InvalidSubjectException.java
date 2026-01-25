package dev.gaurav.nityalog.exceptions;

import org.springframework.security.oauth2.jwt.BadJwtException;

public class InvalidSubjectException extends BadJwtException {
    public InvalidSubjectException(String message) {
        super(message);
    }
}
