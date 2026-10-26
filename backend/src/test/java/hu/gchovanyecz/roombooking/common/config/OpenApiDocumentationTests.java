package hu.gchovanyecz.roombooking.common.config;

import hu.gchovanyecz.roombooking.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTests {

	private static final String PROBLEM_JSON = "application/problem+json";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void describesTheApiWithTitleAndTags() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.info.title").value("Tárgyalófoglaló API"))
			.andExpect(jsonPath("$.info.version").value("1.0.0"))
			.andExpect(jsonPath("$.tags[*].name", containsInAnyOrder("Rooms", "Bookings", "Admin")));
	}

	@Test
	void documentsEveryEndpoint() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.paths['/api/rooms'].get.tags[0]").value("Rooms"))
			.andExpect(jsonPath("$.paths['/api/rooms/{roomId}'].get.tags[0]").value("Rooms"))
			.andExpect(jsonPath("$.paths['/api/bookings'].get.tags[0]").value("Bookings"))
			.andExpect(jsonPath("$.paths['/api/bookings'].post.tags[0]").value("Bookings"))
			.andExpect(jsonPath("$.paths['/api/bookings/{bookingId}'].get.tags[0]").value("Bookings"))
			.andExpect(jsonPath("$.paths['/api/admin/bookings/{bookingId}'].delete.tags[0]").value("Admin"));
	}

	@Test
	void documentsProblemDetailErrorBodies() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.paths['/api/bookings'].post.responses['409'].content['" + PROBLEM_JSON + "'].schema['$ref']")
				.value("#/components/schemas/ProblemDetail"))
			.andExpect(jsonPath("$.paths['/api/bookings'].post.responses['400'].content['" + PROBLEM_JSON + "'].examples.validationErrors.value.errors[0].field")
				.value("startTime"))
			.andExpect(jsonPath("$.paths['/api/bookings'].post.responses['409'].content['" + PROBLEM_JSON + "'].examples.bookingConflict.value.conflictingBookingId")
				.value(3))
			.andExpect(jsonPath("$.paths['/api/rooms/{roomId}'].get.responses['404'].content['" + PROBLEM_JSON + "'].schema['$ref']")
				.value("#/components/schemas/ProblemDetail"))
			.andExpect(jsonPath("$.components.schemas.ProblemDetail.properties.conflictingBookingId").exists())
			.andExpect(jsonPath("$.components.schemas.ProblemDetail.properties.errors").exists());
	}

}
