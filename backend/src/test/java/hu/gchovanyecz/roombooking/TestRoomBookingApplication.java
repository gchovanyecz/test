package hu.gchovanyecz.roombooking;

import org.springframework.boot.SpringApplication;

public class TestRoomBookingApplication {

	public static void main(String[] args) {
		SpringApplication.from(RoomBookingApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
