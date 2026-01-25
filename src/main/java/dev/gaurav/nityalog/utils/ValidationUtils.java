package dev.gaurav.nityalog.utils;

public final class ValidationUtils {
    public static void assertExactlyOneNotNull(Object a, Object b) {
        assertExactlyOneNotNull(a, b, "Exactly one of the given arguments must be non-null.");
    }

    public static void assertExactlyOneNotNull(Object a, Object b, String message) {
        if ((a == null && b == null) || (a != null && b != null)) {
            throw new IllegalArgumentException(message);
        }
    }

}
