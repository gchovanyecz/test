package hu.gchovanyecz.roombooking.room.service;

import hu.gchovanyecz.roombooking.common.exception.ResourceNotFoundException;

public class RoomNotFoundException extends ResourceNotFoundException {

	public RoomNotFoundException(long roomId) {
		super("Nem létezik tárgyaló ezzel az azonosítóval: " + roomId);
	}

}
