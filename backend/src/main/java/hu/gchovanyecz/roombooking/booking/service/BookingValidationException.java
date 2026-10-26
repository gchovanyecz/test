package hu.gchovanyecz.roombooking.booking.service;

import java.util.List;

import hu.gchovanyecz.roombooking.common.exception.FieldViolation;

/**
 * The requested booking breaks a time rule (order, past start, grid, duration); mapped to HTTP 400.
 */
public class BookingValidationException extends RuntimeException {

	private final List<FieldViolation> violations;

	public BookingValidationException(List<FieldViolation> violations) {
		super("A foglalás nem felel meg a foglalási szabályoknak.");
		this.violations = List.copyOf(violations);
	}

	public List<FieldViolation> getViolations() {
		return violations;
	}

}
