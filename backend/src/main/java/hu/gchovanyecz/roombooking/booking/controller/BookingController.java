package hu.gchovanyecz.roombooking.booking.controller;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import hu.gchovanyecz.roombooking.booking.dto.BookingResponse;
import hu.gchovanyecz.roombooking.booking.dto.CreateBookingRequest;
import hu.gchovanyecz.roombooking.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Foglalások leadása és lekérdezése")
public class BookingController {

	private final BookingService bookingService;

	public BookingController(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@GetMapping
	@Operation(summary = "Foglalások listázása",
			description = "Kezdési idő szerint rendezve. Szűrhető tárgyalóra és időszakra; az időszak szűrése "
					+ "a [from, to) intervallummal átfedő foglalásokat adja vissza.")
	@ApiResponse(responseCode = "200", description = "A foglalások")
	@ApiResponse(responseCode = "400", description = "Érvénytelen szűrési időszak")
	@ApiResponse(responseCode = "404", description = "Nem létező tárgyaló")
	public List<BookingResponse> listBookings(
			@Parameter(description = "Csak ennek a tárgyalónak a foglalásai") @RequestParam(required = false) Long roomId,
			@Parameter(description = "Időszak kezdete (ISO-8601)", example = "2030-01-07T00:00:00Z")
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
			@Parameter(description = "Időszak vége, nem része az időszaknak (ISO-8601)", example = "2030-01-08T00:00:00Z")
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
		return bookingService.findAll(roomId, from, to);
	}

	@GetMapping("/{bookingId}")
	@Operation(summary = "Egy foglalás lekérdezése")
	@ApiResponse(responseCode = "200", description = "A foglalás")
	@ApiResponse(responseCode = "404", description = "Nem létezik foglalás ezzel az azonosítóval")
	public BookingResponse getBooking(@PathVariable long bookingId) {
		return bookingService.getById(bookingId);
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
