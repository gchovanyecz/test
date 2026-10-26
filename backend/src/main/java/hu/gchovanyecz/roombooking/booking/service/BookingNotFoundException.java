package hu.gchovanyecz.roombooking.booking.service;

import hu.gchovanyecz.roombooking.common.exception.ResourceNotFoundException;

public class BookingNotFoundException extends ResourceNotFoundException {

	public BookingNotFoundException(long bookingId) {
		super("Nem létezik foglalás ezzel az azonosítóval: " + bookingId);
	}

}
