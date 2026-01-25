package dev.gaurav.nityalog.dtos;

public record ApiError(
        String error,
        String errorDescription,
        String path
) {
   public static ApiError generic(String description, String path) {
        return new ApiError("Server Error", description, path);
    }

    public static ApiError validation(String description, String path) {
        return new ApiError("Validation Error", description, path);
    }

    public static ApiError unauthorized(String description, String path) {
        return new ApiError("Unauthorized", description, path);
    }

    public static ApiError forbidden(String description, String path) {
        return new ApiError("Forbidden", description, path);
    }

    public static ApiError notFound(String description, String path) {
        return new ApiError("Not Found", description, path);
    }

    public static ApiError badRequest(String description, String path) {
        return new ApiError("Bad Request", description, path);
    }

    public static ApiError tooManyRequests(String description, String path) {
        return new ApiError("Too Many Requests", description, path);
    }
}