package dev.gaurav.nityalog.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T> (
        boolean success,
        int status,
        T data,
        ApiError error,
        List<String> warnings,
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, 200, data, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, 201, data, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> success(int statusCode, T data) {
        return new ApiResponse<>(true, statusCode, data, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> success(int statusCode, T data, List<String> warnings) {
        return new ApiResponse<>(true, statusCode, data, null, warnings, Instant.now());
    }

    public static <T> ApiResponse<T> error(int statusCode, ApiError error) {
        return new ApiResponse<>(false, statusCode, null, error, null, Instant.now());
    }

    public static ApiResponse<Void> validationError(ApiError error) {
        return new ApiResponse<>(false, 400, null, error, null, Instant.now());
    }
}
