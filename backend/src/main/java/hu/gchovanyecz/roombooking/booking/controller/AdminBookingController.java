package hu.gchovanyecz.roombooking.booking.controller;

import hu.gchovanyecz.roombooking.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrative booking operations. They live under {@code /api/admin}: without authentication there is no way to
 * know who owns a booking, so deleting one is an administrator's task.
 */
@RestController
@RequestMapping("/api/admin/bookings")
@Tag(name = "Admin", description = "Adminisztrációs műveletek")
public class AdminBookingController {

	private final BookingService bookingService;

	public AdminBookingController(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@DeleteMapping("/{bookingId}")
	@Operation(summary = "Foglalás törlése")
	@ApiResponse(responseCode = "204", description = "A foglalás törölve")
	@ApiResponse(responseCode = "404", description = "Nem létezik foglalás ezzel az azonosítóval")
	public ResponseEntity<Void> deleteBooking(@PathVariable long bookingId) {
		bookingService.delete(bookingId);
		return ResponseEntity.noContent().build();
	}

}
