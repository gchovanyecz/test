package hu.gchovanyecz.roombooking.booking.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Új foglalás. Az időpontok ISO-8601 formátumúak, időzóna-eltolással vagy UTC-ben.")
public record CreateBookingRequest(
		@Schema(example = "1")
		@NotNull(message = "A tárgyaló megadása kötelező.")
		Long roomId,

		@Schema(description = "Opcionális megnevezés", example = "Sprint planning", nullable = true)
		@Size(max = 100, message = "A cím legfeljebb 100 karakter lehet.")
		String title,

		@Schema(description = "Kezdés, 15 perces időrácson", example = "2030-01-07T09:00:00+01:00")
		@NotNull(message = "A kezdés megadása kötelező.")
		Instant startTime,

		@Schema(description = "Befejezés (nem része a foglalásnak), 15 perces időrácson", example = "2030-01-07T10:00:00+01:00")
		@NotNull(message = "A befejezés megadása kötelező.")
		Instant endTime) {
}
