package hu.gchovanyecz.roombooking.common.web;

import java.time.Instant;
import java.util.List;

import hu.gchovanyecz.roombooking.common.exception.FieldViolation;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Documentation-only shape of the RFC 9457 error body ({@code application/problem+json}) that
 * {@link GlobalExceptionHandler} returns. The extra fields are present only for the matching error:
 * {@code errors} for 400 validation failures, the {@code conflicting*} fields for 409 booking conflicts.
 */
@Schema(name = "ProblemDetail", description = "Hibaválasz (RFC 9457, application/problem+json)")
public record ProblemDetailDoc(
		@Schema(example = "about:blank") String type,
		@Schema(example = "Conflict") String title,
		@Schema(example = "409") int status,
		@Schema(example = "A tárgyaló ebben az idősávban már foglalt.") String detail,
		@Schema(description = "Mezőhibák a 400-as válaszban", nullable = true) List<FieldViolation> errors,
		@Schema(description = "Az ütköző foglalás azonosítója (409)", example = "3", nullable = true)
		Long conflictingBookingId,
		@Schema(description = "Az ütköző foglalás kezdete (409)", example = "2030-01-07T07:30:00Z", nullable = true)
		Instant conflictingStartTime,
		@Schema(description = "Az ütköző foglalás vége (409)", example = "2030-01-07T08:30:00Z", nullable = true)
		Instant conflictingEndTime) {
}
