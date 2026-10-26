package hu.gchovanyecz.roombooking.room.controller;

import java.util.List;

import hu.gchovanyecz.roombooking.room.dto.RoomResponse;
import hu.gchovanyecz.roombooking.room.service.RoomNotFoundException;
import hu.gchovanyecz.roombooking.room.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
class RoomControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RoomService roomService;

	@Test
	void listsRooms() throws Exception {
		given(roomService.findAll()).willReturn(List.of(
				new RoomResponse(1, "Budapest", 12, "1. emelet"),
				new RoomResponse(2, "Debrecen", 8, "1. emelet")));

		mockMvc.perform(get("/api/rooms"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].name").value("Budapest"))
			.andExpect(jsonPath("$[0].capacity").value(12))
			.andExpect(jsonPath("$[0].location").value("1. emelet"));
	}

	@Test
	void returnsRoomById() throws Exception {
		given(roomService.getById(1)).willReturn(new RoomResponse(1, "Budapest", 12, "1. emelet"));

		mockMvc.perform(get("/api/rooms/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Budapest"));
	}

	@Test
	void returns404ForUnknownRoom() throws Exception {
		given(roomService.getById(99)).willThrow(new RoomNotFoundException(99));

		mockMvc.perform(get("/api/rooms/99"))
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.detail").value("Nem létezik tárgyaló ezzel az azonosítóval: 99"));
	}

	@Test
	void returns400ForNonNumericRoomId() throws Exception {
		mockMvc.perform(get("/api/rooms/abc"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
	}

}
