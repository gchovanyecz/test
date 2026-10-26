package hu.gchovanyecz.roombooking.room.controller;

import java.util.List;

import hu.gchovanyecz.roombooking.room.dto.RoomResponse;
import hu.gchovanyecz.roombooking.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Rooms", description = "A fix tárgyalólista")
public class RoomController {

	private final RoomService roomService;

	public RoomController(RoomService roomService) {
		this.roomService = roomService;
	}

	@GetMapping
	@Operation(summary = "Tárgyalók listázása", description = "Az összes tárgyaló, név szerint rendezve.")
	public List<RoomResponse> listRooms() {
		return roomService.findAll();
	}

	@GetMapping("/{roomId}")
	@Operation(summary = "Egy tárgyaló lekérdezése")
	@ApiResponse(responseCode = "200", description = "A tárgyaló")
	@ApiResponse(responseCode = "404", description = "Nem létezik tárgyaló ezzel az azonosítóval")
	public RoomResponse getRoom(@PathVariable long roomId) {
		return roomService.getById(roomId);
	}

}
