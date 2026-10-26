package hu.gchovanyecz.roombooking.booking.controller;

import java.net.URI;

import hu.gchovanyecz.roombooking.booking.dto.BookingResponse;
import hu.gchovanyecz.roombooking.booking.dto.CreateBookingRequest;
import hu.gchovanyecz.roombooking.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Foglalások leadása és lekérdezése")
public class BookingController {

	private final BookingService bookingService;

	public BookingController(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@PostMapping
	@Operation(summary = "Foglalás leadása",
			description = "Legalább 15 perc, legfeljebb 8 óra, 15 perces időrácson, nem a múltban, "
					+ "és nem ütközhet a tárgyaló egy meglévő foglalásával.")
	@ApiResponse(responseCode = "201", description = "A foglalás létrejött")
	@ApiResponse(responseCode = "400", description = "Érvénytelen kérés vagy megszegett foglalási szabály")
	@ApiResponse(responseCode = "404", description = "Nem létező tárgyaló")
	@ApiResponse(responseCode = "409", description = "Ütközik egy meglévő foglalással")
	public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
		BookingResponse created = bookingService.create(request);
		return ResponseEntity.created(URI.create("/api/bookings/" + created.id())).body(created);
	}

}
