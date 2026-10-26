package hu.gchovanyecz.roombooking.booking.service;

import java.time.Instant;
import java.util.stream.Stream;

import hu.gchovanyecz.roombooking.TestcontainersConfiguration;
import hu.gchovanyecz.roombooking.booking.dto.BookingResponse;
import hu.gchovanyecz.roombooking.booking.dto.CreateBookingRequest;
import hu.gchovanyecz.roombooking.booking.repository.BookingRepository;
import hu.gchovanyecz.roombooking.room.repository.RoomRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

/**
 * The overlap rule against a real database: the decision is made by the {@code findOverlapping} query, so these cases
 * are not mocked. The existing booking is 10:00–11:00 in Budapest.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class BookingOverlapIntegrationTests {

	@Autowired
	private BookingService bookingService;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private RoomRepository roomRepository;

	private long budapest;

	private BookingResponse existing;

	@BeforeEach
	void createExistingBooking() {
		budapest = roomId("Budapest");
		existing = bookingService.create(new CreateBookingRequest(budapest, "Meglévő", at("10:00"), at("11:00")));
	}

	@AfterEach
	void deleteBookings() {
		bookingRepository.deleteAll();
	}

	static Stream<Arguments> overlappingIntervals() {
		return Stream.of(
				argumentSet("átfedés az elején", "09:30", "10:30"),
				argumentSet("átfedés a végén", "10:30", "11:30"),
				argumentSet("teljesen tartalmazza a meglévőt", "09:00", "12:00"),
				argumentSet("teljesen benne van a meglévőben", "10:15", "10:45"),
				argumentSet("pontosan azonos intervallum", "10:00", "11:00"),
				argumentSet("azonos kezdés, rövidebb", "10:00", "10:15"),
				argumentSet("azonos befejezés, rövidebb", "10:45", "11:00"));
	}

	static Stream<Arguments> freeIntervals() {
		return Stream.of(
				argumentSet("közvetlenül előtte (a vége = a meglévő kezdete)", "09:00", "10:00"),
				argumentSet("közvetlenül utána (a kezdete = a meglévő vége)", "11:00", "12:00"),
				argumentSet("jóval előtte", "07:00", "08:00"),
				argumentSet("jóval utána", "13:00", "14:00"));
	}

	@ParameterizedTest
	@MethodSource("overlappingIntervals")
	void overlappingBookingIsRejected(String start, String end) {
		assertThat(bookingRepository.findOverlapping(budapest, at(start), at(end)))
			.extracting("id")
			.containsExactly(existing.id());

		assertThatThrownBy(() -> bookingService.create(new CreateBookingRequest(budapest, null, at(start), at(end))))
			.isInstanceOfSatisfying(BookingConflictException.class, conflict -> {
				assertThat(conflict.getConflictingBookingId()).isEqualTo(existing.id());
				assertThat(conflict.getConflictingStartTime()).isEqualTo(at("10:00"));
				assertThat(conflict.getConflictingEndTime()).isEqualTo(at("11:00"));
			});
		assertThat(bookingRepository.count()).isEqualTo(1);
	}

	@ParameterizedTest
	@MethodSource("freeIntervals")
	void nonOverlappingBookingIsAccepted(String start, String end) {
		assertThat(bookingRepository.findOverlapping(budapest, at(start), at(end))).isEmpty();

		bookingService.create(new CreateBookingRequest(budapest, null, at(start), at(end)));
		assertThat(bookingRepository.count()).isEqualTo(2);
	}

	@Test
	void sameIntervalInAnotherRoomIsAccepted() {
		long debrecen = roomId("Debrecen");

		assertThat(bookingRepository.findOverlapping(debrecen, at("10:00"), at("11:00"))).isEmpty();
		bookingService.create(new CreateBookingRequest(debrecen, null, at("10:00"), at("11:00")));
		assertThat(bookingRepository.count()).isEqualTo(2);
	}

	@Test
	void reportsTheEarliestOfSeveralConflicts() {
		BookingResponse later = bookingService.create(new CreateBookingRequest(budapest, null, at("11:00"), at("12:00")));

		assertThatThrownBy(() -> bookingService.create(new CreateBookingRequest(budapest, null, at("10:30"), at("11:30"))))
			.isInstanceOfSatisfying(BookingConflictException.class,
					conflict -> assertThat(conflict.getConflictingBookingId()).isEqualTo(existing.id()));
		assertThat(later.id()).isNotEqualTo(existing.id());
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
