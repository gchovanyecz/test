package hu.gchovanyecz.roombooking.booking.controller;

import java.time.Instant;
import java.util.List;

import hu.gchovanyecz.roombooking.booking.dto.BookingResponse;
import hu.gchovanyecz.roombooking.booking.dto.CreateBookingRequest;
import hu.gchovanyecz.roombooking.booking.service.BookingConflictException;
import hu.gchovanyecz.roombooking.booking.service.BookingNotFoundException;
import hu.gchovanyecz.roombooking.booking.service.BookingService;
import hu.gchovanyecz.roombooking.booking.service.BookingValidationException;
import hu.gchovanyecz.roombooking.common.exception.FieldViolation;
import hu.gchovanyecz.roombooking.room.service.RoomNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTests {

	private static final Instant NINE_UTC = Instant.parse("2030-01-07T08:00:00Z");
	private static final Instant TEN_UTC = Instant.parse("2030-01-07T09:00:00Z");

	private static final String VALID_BODY = """
			{ "roomId": 1, "title": "Sprint planning",
			  "startTime": "2030-01-07T09:00:00+01:00", "endTime": "2030-01-07T10:00:00+01:00" }
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BookingService bookingService;

	@Test
	void createsBookingAndReturnsLocation() throws Exception {
		given(bookingService.create(any())).willReturn(new BookingResponse(5, 1, "Budapest", "Sprint planning",
				NINE_UTC, TEN_UTC, Instant.parse("2030-01-01T10:00:00Z")));

		mockMvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", "/api/bookings/5"))
			.andExpect(jsonPath("$.id").value(5))
			.andExpect(jsonPath("$.roomId").value(1))
			.andExpect(jsonPath("$.roomName").value("Budapest"))
			.andExpect(jsonPath("$.title").value("Sprint planning"))
			.andExpect(jsonPath("$.startTime").value("2030-01-07T08:00:00Z"))
			.andExpect(jsonPath("$.endTime").value("2030-01-07T09:00:00Z"));
	}

	@Test
	void convertsTimesWithOffsetToUtcInstants() throws Exception {
		given(bookingService.create(any())).willReturn(new BookingResponse(5, 1, "Budapest", null,
				NINE_UTC, TEN_UTC, Instant.parse("2030-01-01T10:00:00Z")));

		mockMvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
			.andExpect(status().isCreated());

		verify(bookingService).create(new CreateBookingRequest(1L, "Sprint planning", NINE_UTC, TEN_UTC));
	}

	@Test
	void rejectsMissingFieldsWithoutCallingTheService() throws Exception {
		mockMvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.errors.length()").value(3))
			.andExpect(jsonPath("$.errors[?(@.field == 'roomId')].message").value("A tárgyaló megadása kötelező."))
			.andExpect(jsonPath("$.errors[?(@.field == 'startTime')]").exists())
			.andExpect(jsonPath("$.errors[?(@.field == 'endTime')]").exists());
		verifyNoInteractions(bookingService);
	}

	@Test
	void rejectsTooLongTitle() throws Exception {
		String body = VALID_BODY.replace("Sprint planning", "x".repeat(101));

		mockMvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field").value("title"));
	}

	@Test
	void returns400WithFieldErrorsWhenTimeRulesAreBroken() throws Exception {
		given(bookingService.create(any())).willThrow(new BookingValidationException(
				List.of(new FieldViolation("startTime", "A kezdés nem lehet a múltban."))));

		mockMvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.detail").value("A foglalás nem felel meg a foglalási szabályoknak."))
			.andExpect(jsonPath("$.errors[0].field").value("startTime"))
			.andExpect(jsonPath("$.errors[0].message").value("A kezdés nem lehet a múltban."));
	}

	@Test
	void returns404ForUnknownRoom() throws Exception {
		given(bookingService.create(any())).willThrow(new RoomNotFoundException(1));

		mockMvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
			.andExpect(status().isNotFound());
	}

	@Test
	void returns409ForConflict() throws Exception {
		given(bookingService.create(any())).willThrow(new BookingConflictException(3, NINE_UTC, TEN_UTC));

		mockMvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.conflictingBookingId").value(3))
			.andExpect(jsonPath("$.conflictingStartTime").value("2030-01-07T08:00:00Z"));
	}

	@Test
	void listsBookingsWithoutFilters() throws Exception {
		given(bookingService.findAll(null, null, null)).willReturn(List.of(new BookingResponse(5, 1, "Budapest", null,
				NINE_UTC, TEN_UTC, Instant.parse("2030-01-01T10:00:00Z"))));

		mockMvc.perform(get("/api/bookings"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].id").value(5))
			.andExpect(jsonPath("$[0].title").isEmpty());
	}

	@Test
	void passesFiltersToTheService() throws Exception {
		given(bookingService.findAll(any(), any(), any())).willReturn(List.of());

		mockMvc.perform(get("/api/bookings")
				.param("roomId", "1")
				.param("from", "2030-01-07T00:00:00+01:00")
				.param("to", "2030-01-08T00:00:00Z"))
			.andExpect(status().isOk());

		verify(bookingService).findAll(1L, Instant.parse("2030-01-06T23:00:00Z"), Instant.parse("2030-01-08T00:00:00Z"));
	}

	@Test
	void rejectsMalformedTimeFilter() throws Exception {
		mockMvc.perform(get("/api/bookings").param("from", "tegnap"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
		verifyNoInteractions(bookingService);
	}

	@Test
	void returnsBookingById() throws Exception {
		given(bookingService.getById(5)).willReturn(new BookingResponse(5, 1, "Budapest", "Demo",
				NINE_UTC, TEN_UTC, Instant.parse("2030-01-01T10:00:00Z")));

		mockMvc.perform(get("/api/bookings/5"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("Demo"));
	}

	@Test
	void returns404ForUnknownBooking() throws Exception {
		given(bookingService.getById(99)).willThrow(new BookingNotFoundException(99));

		mockMvc.perform(get("/api/bookings/99"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail").value("Nem létezik foglalás ezzel az azonosítóval: 99"));
	}

	@Test
	void deleteIsNotAvailableOnThePublicPath() throws Exception {
		mockMvc.perform(delete("/api/bookings/5"))
			.andExpect(status().isMethodNotAllowed());
		verifyNoInteractions(bookingService);
	}

}
