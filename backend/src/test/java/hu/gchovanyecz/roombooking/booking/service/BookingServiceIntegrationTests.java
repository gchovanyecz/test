package hu.gchovanyecz.roombooking.booking.service;

import java.time.Instant;

import hu.gchovanyecz.roombooking.TestcontainersConfiguration;
import hu.gchovanyecz.roombooking.booking.domain.Booking;
import hu.gchovanyecz.roombooking.booking.dto.BookingResponse;
import hu.gchovanyecz.roombooking.booking.dto.CreateBookingRequest;
import hu.gchovanyecz.roombooking.booking.repository.BookingRepository;
import hu.gchovanyecz.roombooking.room.domain.Room;
import hu.gchovanyecz.roombooking.room.repository.RoomRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs the booking service against a real PostgreSQL database with the real (system clock) time rules.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class BookingServiceIntegrationTests {

	private static final Instant NINE = Instant.parse("2099-01-05T09:00:00Z");
	private static final Instant TEN = Instant.parse("2099-01-05T10:00:00Z");
	private static final Instant ELEVEN = Instant.parse("2099-01-05T11:00:00Z");

	@Autowired
	private BookingService bookingService;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private RoomRepository roomRepository;

	@AfterEach
	void deleteBookings() {
		bookingRepository.deleteAll();
	}

	@Test
	void createsBookingWithCreationTimestamp() {
		BookingResponse created = bookingService.create(new CreateBookingRequest(roomId("Budapest"), "Demo", NINE, TEN));

		assertThat(created.roomName()).isEqualTo("Budapest");
		assertThat(created.createdAt()).isNotNull();
		assertThat(bookingRepository.findById(created.id())).isPresent();
	}

	@Test
	void rejectsOverlapAndAllowsAdjacentBooking() {
		long roomId = roomId("Budapest");
		BookingResponse first = bookingService.create(new CreateBookingRequest(roomId, null, NINE, TEN));

		assertThatThrownBy(() -> bookingService.create(new CreateBookingRequest(roomId, null, NINE.plusSeconds(1800), ELEVEN)))
			.isInstanceOfSatisfying(BookingConflictException.class,
					conflict -> assertThat(conflict.getConflictingBookingId()).isEqualTo(first.id()));

		bookingService.create(new CreateBookingRequest(roomId, null, TEN, ELEVEN));
		assertThat(bookingRepository.count()).isEqualTo(2);
	}

	@Test
	void databaseConstraintViolationNamesTheConstraintForTheErrorHandler() {
		Room room = roomRepository.findById(roomId("Budapest")).orElseThrow();
		bookingRepository.saveAndFlush(new Booking(room, null, NINE, ELEVEN));

		// Bypasses the service check, as a concurrent request would; the handler maps this message to 409.
		assertThatThrownBy(() -> bookingRepository.saveAndFlush(new Booking(room, null, TEN, ELEVEN)))
			.isInstanceOf(DataIntegrityViolationException.class)
			.hasMessageContaining("booking_no_overlap");
	}

	private long roomId(String name) {
		return roomRepository.findAllByOrderByNameAsc().stream()
			.filter(room -> room.getName().equals(name))
			.findFirst()
			.orElseThrow()
			.getId();
	}

}
