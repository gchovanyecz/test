package hu.gchovanyecz.roombooking.booking.dto;

import java.time.Instant;

import hu.gchovanyecz.roombooking.booking.domain.Booking;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Egy foglalás. Az időpontok UTC-ben (ISO-8601) érkeznek.")
public record BookingResponse(
		@Schema(example = "1") long id,
		@Schema(example = "1") long roomId,
		@Schema(example = "Budapest") String roomName,
		@Schema(example = "Sprint planning", nullable = true) String title,
		@Schema(example = "2030-01-07T08:00:00Z") Instant startTime,
		@Schema(example = "2030-01-07T09:00:00Z") Instant endTime,
		@Schema(example = "2030-01-01T10:15:30Z") Instant createdAt) {

	public static BookingResponse from(Booking booking) {
		return new BookingResponse(booking.getId(), booking.getRoom().getId(), booking.getRoom().getName(),
				booking.getTitle(), booking.getStartTime(), booking.getEndTime(), booking.getCreatedAt());
	}

}
