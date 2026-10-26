package hu.gchovanyecz.roombooking.booking.repository;

import java.time.Instant;

import hu.gchovanyecz.roombooking.booking.domain.Booking;
import org.springframework.data.jpa.domain.Specification;

/**
 * Optional filters for listing bookings; each is unrestricted when its argument is missing.
 */
public final class BookingSpecifications {

	private BookingSpecifications() {
	}

	public static Specification<Booking> inRoom(Long roomId) {
		if (roomId == null) {
			return Specification.unrestricted();
		}
		return (root, query, cb) -> cb.equal(root.get("room").get("id"), roomId);
	}

	/** Bookings still running after {@code from}. */
	public static Specification<Booking> endingAfter(Instant from) {
		if (from == null) {
			return Specification.unrestricted();
		}
		return (root, query, cb) -> cb.greaterThan(root.get("endTime"), from);
	}

	/** Bookings starting before {@code to}. */
	public static Specification<Booking> startingBefore(Instant to) {
		if (to == null) {
			return Specification.unrestricted();
		}
		return (root, query, cb) -> cb.lessThan(root.get("startTime"), to);
	}

}
