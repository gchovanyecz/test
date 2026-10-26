package hu.gchovanyecz.roombooking.common.web;

import java.time.Instant;

import hu.gchovanyecz.roombooking.booking.service.BookingConflictException;
import hu.gchovanyecz.roombooking.room.service.RoomNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTests {

	private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
		.setControllerAdvice(new GlobalExceptionHandler())
		.build();

	@Test
	void notFoundIsMappedTo404() throws Exception {
		mockMvc.perform(get("/test/room-not-found"))
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.detail").value("Nem létezik tárgyaló ezzel az azonosítóval: 42"));
	}

	@Test
	void bookingConflictIsMappedTo409WithConflictingBooking() throws Exception {
		mockMvc.perform(get("/test/booking-conflict"))
			.andExpect(status().isConflict())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.detail").value("A tárgyaló ebben az idősávban már foglalt."))
			.andExpect(jsonPath("$.conflictingBookingId").value(7))
			.andExpect(jsonPath("$.conflictingStartTime").exists())
			.andExpect(jsonPath("$.conflictingEndTime").exists());
	}

	@Test
	void overlapConstraintViolationIsMappedTo409() throws Exception {
		mockMvc.perform(get("/test/overlap-constraint"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.detail").value("A tárgyaló ebben az idősávban már foglalt."));
	}

	@Test
	void otherDataIntegrityViolationIsMappedTo500() throws Exception {
		mockMvc.perform(get("/test/other-constraint"))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.detail").value("Váratlan hiba történt."));
	}

	@Test
	void invalidBodyIsMappedTo400WithFieldErrors() throws Exception {
		mockMvc.perform(post("/test/validated").contentType(MediaType.APPLICATION_JSON).content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.detail").value("A kérés érvénytelen mezőket tartalmaz."))
			.andExpect(jsonPath("$.errors[0].field").value("roomId"))
			.andExpect(jsonPath("$.errors[0].message").exists());
	}

	@Test
	void malformedJsonIsMappedTo400() throws Exception {
		mockMvc.perform(post("/test/validated").contentType(MediaType.APPLICATION_JSON).content("{ not json"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.detail").value("A kérés törzse nem olvasható (hibás JSON vagy érvénytelen dátumformátum)."));
	}

	@Test
	void unexpectedErrorIsMappedTo500WithoutInternalDetails() throws Exception {
		mockMvc.perform(get("/test/unexpected"))
			.andExpect(status().isInternalServerError())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.detail").value("Váratlan hiba történt."))
			.andExpect(content().string(not(containsString("secret"))));
	}

	/**
	 * Nested in a test class, so Spring Boot's test type filter keeps it out of component scanning in other tests.
	 */
	@RestController
	static class ThrowingController {

		@GetMapping("/test/room-not-found")
		void roomNotFound() {
			throw new RoomNotFoundException(42);
		}

		@GetMapping("/test/booking-conflict")
		void bookingConflict() {
			throw new BookingConflictException(7, Instant.parse("2030-01-07T09:00:00Z"), Instant.parse("2030-01-07T10:00:00Z"));
		}

		@GetMapping("/test/overlap-constraint")
		void overlapConstraint() {
			throw new DataIntegrityViolationException("ERROR: conflicting key value violates exclusion constraint \"booking_no_overlap\"");
		}

		@GetMapping("/test/other-constraint")
		void otherConstraint() {
			throw new DataIntegrityViolationException("ERROR: null value in column \"name\"");
		}

		@PostMapping("/test/validated")
		void validated(@Valid @RequestBody ValidatedRequest request) {
		}

		@GetMapping("/test/unexpected")
		void unexpected() {
			throw new IllegalStateException("secret internal detail");
		}

	}

	record ValidatedRequest(@NotNull Long roomId) {
	}

}
