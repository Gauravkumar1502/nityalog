package dev.gaurav.nityalog.exceptions;

import dev.gaurav.nityalog.dtos.ApiError;
import dev.gaurav.nityalog.dtos.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailSendException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ApiError error = ApiError.validation(message, request.getRequestURI());
        return ResponseEntity.badRequest().body(ApiResponse.validationError(error));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        ApiError error = ApiError.validation(message, request.getRequestURI());
        return ResponseEntity.badRequest().body(ApiResponse.validationError(error));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        ApiError error = ApiError.badRequest(ex.getMostSpecificCause().getMessage(), request.getRequestURI());
        return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), error));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError("Email already in use.", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(HttpStatus.CONFLICT.value(), error));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request
    ) {
        ApiError error = ApiError.notFound(ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), error));
    }

    @ExceptionHandler({
            InvalidOtpException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        ApiError error = ApiError.badRequest(ex.getMessage(), request.getRequestURI());
        return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), error));
    }

    @ExceptionHandler({
            TooManyRequestsException.class,
            LimitExceededException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleTooManyRequests(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        ApiError error = ApiError.tooManyRequests(ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error(HttpStatus.TOO_MANY_REQUESTS.value(), error));
    }

    @ExceptionHandler({
            TokenExpiredException.class,
            TokenNotActiveException.class,
            TokenIssuedInFutureException.class,
            InvalidIssuerException.class,
            InvalidAudienceException.class,
            InvalidSubjectException.class,
            BadJwtException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        ApiError error = ApiError.unauthorized(ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), error));
    }

    @ExceptionHandler(MailSendException.class)
    public ResponseEntity<ApiResponse<Void>> handleMailSend(
            MailSendException ex,
            HttpServletRequest request
    ) {
        ApiError error = ApiError.generic(ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        ApiError error = ApiError.generic(ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), error));
    }
}
