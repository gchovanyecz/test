package hu.gchovanyecz.roombooking.room.service;

import hu.gchovanyecz.roombooking.TestcontainersConfiguration;
import hu.gchovanyecz.roombooking.room.dto.RoomResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class RoomServiceIntegrationTests {

	@Autowired
	private RoomService roomService;

	@Test
	void returnsSeededRoomsSortedByName() {
		assertThat(roomService.findAll())
			.extracting(RoomResponse::name)
			.hasSize(5)
			.isSortedAccordingTo(String::compareTo)
			.contains("Budapest", "Debrecen", "Szeged", "Pécs", "Győr");
	}

	@Test
	void throwsForUnknownRoom() {
		assertThatThrownBy(() -> roomService.getById(999_999))
			.isInstanceOf(RoomNotFoundException.class);
	}

}
