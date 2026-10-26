package hu.gchovanyecz.roombooking.booking.service;

import java.util.List;

import hu.gchovanyecz.roombooking.booking.domain.Booking;
import hu.gchovanyecz.roombooking.booking.dto.BookingResponse;
import hu.gchovanyecz.roombooking.booking.dto.CreateBookingRequest;
import hu.gchovanyecz.roombooking.booking.repository.BookingRepository;
import hu.gchovanyecz.roombooking.room.domain.Room;
import hu.gchovanyecz.roombooking.room.repository.RoomRepository;
import hu.gchovanyecz.roombooking.room.service.RoomNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BookingService {

	private final BookingRepository bookingRepository;

	private final RoomRepository roomRepository;

	private final BookingTimeRules timeRules;

	public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository,
			BookingTimeRules timeRules) {
		this.bookingRepository = bookingRepository;
		this.roomRepository = roomRepository;
		this.timeRules = timeRules;
	}

	/**
	 * Creates a booking if it satisfies the time rules and does not overlap another booking of the same room.
	 * <p>
	 * The overlap check gives a descriptive 409 response; the database exclusion constraint still guards against
	 * two concurrent requests that both pass this check.
	 *
	 * @throws BookingValidationException if a time rule is broken
	 * @throws RoomNotFoundException if the room does not exist
	 * @throws BookingConflictException if the interval overlaps an existing booking of the room
	 */
	@Transactional
	public BookingResponse create(CreateBookingRequest request) {
		timeRules.validate(request.startTime(), request.endTime());

		Room room = roomRepository.findById(request.roomId())
			.orElseThrow(() -> new RoomNotFoundException(request.roomId()));

		List<Booking> overlapping = bookingRepository.findOverlapping(room.getId(), request.startTime(),
				request.endTime());
		if (!overlapping.isEmpty()) {
			Booking conflict = overlapping.getFirst();
			throw new BookingConflictException(conflict.getId(), conflict.getStartTime(), conflict.getEndTime());
		}

		Booking booking = new Booking(room, normalizeTitle(request.title()), request.startTime(), request.endTime());
		// Flush now so that an exclusion constraint violation surfaces here, not at commit time.
		return BookingResponse.from(bookingRepository.saveAndFlush(booking));
	}

	private static String normalizeTitle(String title) {
		return title == null || title.isBlank() ? null : title.strip();
	}

}
