package hu.gchovanyecz.roombooking.booking.service;

import java.time.Instant;
import java.util.List;

import hu.gchovanyecz.roombooking.booking.domain.Booking;
import hu.gchovanyecz.roombooking.booking.dto.BookingResponse;
import hu.gchovanyecz.roombooking.booking.dto.CreateBookingRequest;
import hu.gchovanyecz.roombooking.booking.repository.BookingRepository;
import hu.gchovanyecz.roombooking.common.exception.FieldViolation;
import hu.gchovanyecz.roombooking.room.domain.Room;
import hu.gchovanyecz.roombooking.room.repository.RoomRepository;
import hu.gchovanyecz.roombooking.room.service.RoomNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static hu.gchovanyecz.roombooking.booking.repository.BookingSpecifications.endingAfter;
import static hu.gchovanyecz.roombooking.booking.repository.BookingSpecifications.inRoom;
import static hu.gchovanyecz.roombooking.booking.repository.BookingSpecifications.startingBefore;

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
	 * Lists bookings ordered by start time, optionally restricted to a room and to those overlapping
	 * {@code [from, to)}.
	 *
	 * @throws RoomNotFoundException if {@code roomId} is given but the room does not exist
	 * @throws BookingValidationException if both {@code from} and {@code to} are given and {@code to} is not after
	 * {@code from}
	 */
	public List<BookingResponse> findAll(Long roomId, Instant from, Instant to) {
		if (roomId != null && !roomRepository.existsById(roomId)) {
			throw new RoomNotFoundException(roomId);
		}
		if (from != null && to != null && !to.isAfter(from)) {
			throw new BookingValidationException("Érvénytelen szűrési időszak.",
					List.of(new FieldViolation("to", "A szűrés végének a kezdete után kell lennie.")));
		}

		Specification<Booking> filter = Specification.allOf(inRoom(roomId), endingAfter(from), startingBefore(to));
		return bookingRepository.findAll(filter, Sort.by("startTime", "id")).stream()
			.map(BookingResponse::from)
			.toList();
	}

	/**
	 * @throws BookingNotFoundException if the booking does not exist
	 */
	public BookingResponse getById(long bookingId) {
		return bookingRepository.findById(bookingId)
			.map(BookingResponse::from)
			.orElseThrow(() -> new BookingNotFoundException(bookingId));
	}

	/**
	 * Creates a booking if it satisfies the time rules and does not overlap another booking of the same room.
	 * <p>
	 * The room row is locked first, so concurrent requests for the same room run one after the other and the loser
	 * sees the winner's booking in the overlap check (409 with the conflicting booking). Without the lock, concurrent
	 * inserts can deadlock in PostgreSQL while checking the exclusion constraint. The constraint stays as the final
	 * guarantee against overlaps.
	 *
	 * @throws BookingValidationException if a time rule is broken
	 * @throws RoomNotFoundException if the room does not exist
	 * @throws BookingConflictException if the interval overlaps an existing booking of the room
	 */
	@Transactional
	public BookingResponse create(CreateBookingRequest request) {
		timeRules.validate(request.startTime(), request.endTime());

		Room room = roomRepository.findByIdForUpdate(request.roomId())
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

	/**
	 * @throws BookingNotFoundException if the booking does not exist
	 */
	@Transactional
	public void delete(long bookingId) {
		Booking booking = bookingRepository.findById(bookingId)
			.orElseThrow(() -> new BookingNotFoundException(bookingId));
		bookingRepository.delete(booking);
	}

	private static String normalizeTitle(String title) {
		return title == null || title.isBlank() ? null : title.strip();
	}

}
