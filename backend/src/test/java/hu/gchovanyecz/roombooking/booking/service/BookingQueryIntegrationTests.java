package hu.gchovanyecz.roombooking.booking.service;

import java.time.Instant;

import hu.gchovanyecz.roombooking.TestcontainersConfiguration;
import hu.gchovanyecz.roombooking.booking.dto.BookingResponse;
import hu.gchovanyecz.roombooking.booking.dto.CreateBookingRequest;
import hu.gchovanyecz.roombooking.booking.repository.BookingRepository;
import hu.gchovanyecz.roombooking.room.repository.RoomRepository;
import hu.gchovanyecz.roombooking.room.service.RoomNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Listing, lookup and deletion of bookings against a real PostgreSQL database.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class BookingQueryIntegrationTests {

	@Autowired
	private BookingService bookingService;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private RoomRepository roomRepository;

	private long budapest;

	private long debrecen;

	private BookingResponse budapestAfternoon;

	private BookingResponse budapestMorning;

	private BookingResponse debrecenNoon;

	@BeforeEach
	void createBookings() {
		budapest = roomId("Budapest");
		debrecen = roomId("Debrecen");
		// Created out of order to verify sorting by start time.
		budapestAfternoon = book(budapest, "14:00", "15:00");
		budapestMorning = book(budapest, "09:00", "10:00");
		debrecenNoon = book(debrecen, "12:00", "13:00");
	}

	@AfterEach
	void deleteBookings() {
		bookingRepository.deleteAll();
	}

	@Test
	void listsAllBookingsSortedByStartTime() {
		assertThat(bookingService.findAll(null, null, null))
			.extracting(BookingResponse::id)
			.containsExactly(budapestMorning.id(), debrecenNoon.id(), budapestAfternoon.id());
	}

	@Test
	void filtersByRoom() {
		assertThat(bookingService.findAll(budapest, null, null))
			.extracting(BookingResponse::id)
			.containsExactly(budapestMorning.id(), budapestAfternoon.id());
	}

	@Test
	void filtersByOverlapWithTimeRange() {
		// [09:30, 12:00) overlaps the morning booking but not the one starting exactly at 12:00.
		assertThat(bookingService.findAll(null, at("09:30"), at("12:00")))
			.extracting(BookingResponse::id)
			.containsExactly(budapestMorning.id());
		// Open-ended ranges.
		assertThat(bookingService.findAll(null, at("13:00"), null))
			.extracting(BookingResponse::id)
			.containsExactly(budapestAfternoon.id());
		assertThat(bookingService.findAll(null, null, at("10:00")))
			.extracting(BookingResponse::id)
			.containsExactly(budapestMorning.id());
	}

	@Test
	void rejectsUnknownRoomFilter() {
		assertThatThrownBy(() -> bookingService.findAll(999_999L, null, null))
			.isInstanceOf(RoomNotFoundException.class);
	}

	@Test
	void rejectsEmptyTimeRange() {
		assertThatThrownBy(() -> bookingService.findAll(null, at("10:00"), at("10:00")))
			.isInstanceOfSatisfying(BookingValidationException.class,
					ex -> assertThat(ex.getViolations()).extracting("field").containsExactly("to"));
	}

	@Test
	void returnsBookingById() {
		assertThat(bookingService.getById(debrecenNoon.id()))
			.isEqualTo(debrecenNoon);
	}

	@Test
	void throwsForUnknownBooking() {
		assertThatThrownBy(() -> bookingService.getById(999_999L))
			.isInstanceOf(BookingNotFoundException.class);
	}

	@Test
	void deletedBookingFreesTheTimeSlot() {
		bookingService.delete(budapestMorning.id());

		assertThat(bookingRepository.existsById(budapestMorning.id())).isFalse();
		assertThat(book(budapest, "09:00", "10:00")).isNotNull();
	}

	@Test
	void deletingUnknownBookingThrows() {
		assertThatThrownBy(() -> bookingService.delete(999_999L))
			.isInstanceOf(BookingNotFoundException.class);
	}

	private BookingResponse book(long roomId, String start, String end) {
		return bookingService.create(new CreateBookingRequest(roomId, null, at(start), at(end)));
	}

	private static Instant at(String utcTime) {
		return Instant.parse("2099-01-05T" + utcTime + ":00Z");
	}

	private long roomId(String name) {
		return roomRepository.findAllByOrderByNameAsc().stream()
			.filter(room -> room.getName().equals(name))
			.findFirst()
			.orElseThrow()
			.getId();
	}

}
