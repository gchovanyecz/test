package hu.gchovanyecz.roombooking.room.repository;

import java.util.List;
import java.util.Optional;

import hu.gchovanyecz.roombooking.room.domain.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, Long> {

	List<Room> findAllByOrderByNameAsc();

	/**
	 * Loads the room with a row lock ({@code SELECT ... FOR UPDATE}) held until the transaction ends, so that bookings
	 * of the same room are created one after the other.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select r from Room r where r.id = :roomId")
	Optional<Room> findByIdForUpdate(@Param("roomId") long roomId);

}
