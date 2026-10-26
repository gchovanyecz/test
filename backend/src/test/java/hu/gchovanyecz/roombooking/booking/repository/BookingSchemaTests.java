package hu.gchovanyecz.roombooking.booking.repository;

import java.sql.Timestamp;
import java.time.Instant;

import hu.gchovanyecz.roombooking.TestcontainersConfiguration;
import hu.gchovanyecz.roombooking.booking.domain.Booking;
import hu.gchovanyecz.roombooking.room.domain.Room;
import hu.gchovanyecz.roombooking.room.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the migrations: seeded rooms, entity mapping and the database-level overlap guarantee.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class BookingSchemaTests {

	private static final Instant NINE = Instant.parse("2030-01-07T09:00:00Z");
	private static final Instant TEN = Instant.parse("2030-01-07T10:00:00Z");
	private static final Instant ELEVEN = Instant.parse("2030-01-07T11:00:00Z");

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private JdbcClient jdbcClient;

	@Test
	void seededRoomsAreAvailable() {
		assertThat(roomRepository.findAllByOrderByNameAsc())
			.extracting(Room::getName)
			.containsExactlyInAnyOrder("Budapest", "Debrecen", "Szeged", "Pécs", "Győr");
	}

	@Test
	void bookingIsPersistedWithCreationTimestamp() {
		Booking saved = bookingRepository.saveAndFlush(new Booking(room("Budapest"), "Sprint planning", NINE, TEN));

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getCreatedAt()).isNotNull();
	}

	@Test
	void overlappingBookingInSameRoomIsRejectedByDatabase() {
		long roomId = room("Budapest").getId();
		insertBooking(roomId, NINE, ELEVEN);

		assertThatThrownBy(() -> insertBooking(roomId, TEN, ELEVEN))
			.isInstanceOf(DataIntegrityViolationException.class)
			.hasMessageContaining("booking_no_overlap");
	}

	@Test
	void adjacentBookingsInSameRoomAreAllowed() {
		long roomId = room("Budapest").getId();
		insertBooking(roomId, NINE, TEN);
		insertBooking(roomId, TEN, ELEVEN);

		assertThat(bookingRepository.count()).isEqualTo(2);
	}

	@Test
	void overlappingBookingsInDifferentRoomsAreAllowed() {
		insertBooking(room("Budapest").getId(), NINE, ELEVEN);
		insertBooking(room("Debrecen").getId(), NINE, ELEVEN);

		assertThat(bookingRepository.count()).isEqualTo(2);
	}

	@Test
	void bookingEndingBeforeItStartsIsRejectedByDatabase() {
		long roomId = room("Budapest").getId();

		assertThatThrownBy(() -> insertBooking(roomId, TEN, NINE))
			.isInstanceOf(DataIntegrityViolationException.class)
			.hasMessageContaining("booking_time_range_valid");
	}

	private Room room(String name) {
		return roomRepository.findAll().stream()
			.filter(room -> room.getName().equals(name))
			.findFirst()
			.orElseThrow();
	}

	private void insertBooking(long roomId, Instant start, Instant end) {
		jdbcClient.sql("""
				insert into booking (room_id, start_time, end_time, created_at)
				values (:roomId, :start, :end, now())
				""")
			.param("roomId", roomId)
			.param("start", Timestamp.from(start))
			.param("end", Timestamp.from(end))
			.update();
	}

}
