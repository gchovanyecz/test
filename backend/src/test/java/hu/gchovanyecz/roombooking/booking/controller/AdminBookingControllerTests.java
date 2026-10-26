package hu.gchovanyecz.roombooking.booking.controller;

import hu.gchovanyecz.roombooking.booking.service.BookingNotFoundException;
import hu.gchovanyecz.roombooking.booking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminBookingController.class)
class AdminBookingControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BookingService bookingService;

	@Test
	void deletesBooking() throws Exception {
		mockMvc.perform(delete("/api/admin/bookings/5"))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		verify(bookingService).delete(5);
	}

	@Test
	void returns404ForUnknownBooking() throws Exception {
		willThrow(new BookingNotFoundException(99)).given(bookingService).delete(99);

		mockMvc.perform(delete("/api/admin/bookings/99"))
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
	}

}
