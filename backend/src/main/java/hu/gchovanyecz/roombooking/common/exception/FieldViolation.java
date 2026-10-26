package hu.gchovanyecz.roombooking.common.exception;

/**
 * A validation error of a single request field, returned in the {@code errors} list of a 400 response.
 */
public record FieldViolation(String field, String message) {
}
