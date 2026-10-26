package hu.gchovanyecz.roombooking.booking.service;

import java.util.List;

import hu.gchovanyecz.roombooking.common.exception.FieldViolation;

/**
 * The booking request or listing filter breaks a time rule (order, past start, grid, duration); mapped to HTTP 400.
 */
public class BookingValidationException extends RuntimeException {

	private final List<FieldViolation> violations;

	public BookingValidationException(List<FieldViolation> violations) {
		this("A foglalás nem felel meg a foglalási szabályoknak.", violations);
	}

	public BookingValidationException(String message, List<FieldViolation> violations) {
		super(message);
		this.violations = List.copyOf(violations);
	}

	public List<FieldViolation> getViolations() {
		return violations;
	}

}
