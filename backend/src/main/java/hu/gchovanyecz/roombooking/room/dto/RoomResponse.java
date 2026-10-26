package hu.gchovanyecz.roombooking.room.dto;

import hu.gchovanyecz.roombooking.room.domain.Room;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Egy foglalható tárgyaló")
public record RoomResponse(
		@Schema(example = "1") long id,
		@Schema(example = "Budapest") String name,
		@Schema(description = "Férőhelyek száma", example = "12") int capacity,
		@Schema(example = "1. emelet") String location) {

	public static RoomResponse from(Room room) {
		return new RoomResponse(room.getId(), room.getName(), room.getCapacity(), room.getLocation());
	}

}
