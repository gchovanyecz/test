package hu.gchovanyecz.roombooking.room.service;

import java.util.List;

import hu.gchovanyecz.roombooking.room.dto.RoomResponse;
import hu.gchovanyecz.roombooking.room.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RoomService {

	private final RoomRepository roomRepository;

	public RoomService(RoomRepository roomRepository) {
		this.roomRepository = roomRepository;
	}

	public List<RoomResponse> findAll() {
		return roomRepository.findAllByOrderByNameAsc().stream()
			.map(RoomResponse::from)
			.toList();
	}

	public RoomResponse getById(long roomId) {
		return roomRepository.findById(roomId)
			.map(RoomResponse::from)
			.orElseThrow(() -> new RoomNotFoundException(roomId));
	}

}
