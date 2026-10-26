package hu.gchovanyecz.roombooking.booking.repository;

import java.time.Instant;
import java.util.List;

import hu.gchovanyecz.roombooking.booking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

	/**
	 * Bookings of the room that overlap the half-open interval {@code [startTime, endTime)}, earliest first.
	 */
	@Query("""
			select b from Booking b
			where b.room.id = :roomId
			  and b.startTime < :endTime
			  and b.endTime > :startTime
			order by b.startTime
			""")
	List<Booking> findOverlapping(@Param("roomId") long roomId, @Param("startTime") Instant startTime,
			@Param("endTime") Instant endTime);

}
