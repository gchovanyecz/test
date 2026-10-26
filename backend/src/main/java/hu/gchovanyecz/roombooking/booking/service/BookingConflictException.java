package hu.gchovanyecz.roombooking.booking.service;

import java.time.Instant;

/**
 * The requested interval overlaps an existing booking of the same room; mapped to HTTP 409.
 */
public class BookingConflictException extends RuntimeException {

	private final long conflictingBookingId;

	private final Instant conflictingStartTime;

	private final Instant conflictingEndTime;

	public BookingConflictException(long conflictingBookingId, Instant conflictingStartTime, Instant conflictingEndTime) {
		super("A tárgyaló ebben az idősávban már foglalt.");
		this.conflictingBookingId = conflictingBookingId;
		this.conflictingStartTime = conflictingStartTime;
		this.conflictingEndTime = conflictingEndTime;
	}

	public long getConflictingBookingId() {
		return conflictingBookingId;
	}

	public Instant getConflictingStartTime() {
		return conflictingStartTime;
	}

	public Instant getConflictingEndTime() {
		return conflictingEndTime;
	}

}
