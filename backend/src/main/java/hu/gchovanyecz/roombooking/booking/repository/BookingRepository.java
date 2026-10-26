package hu.gchovanyecz.roombooking.booking.repository;

import hu.gchovanyecz.roombooking.booking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

}
