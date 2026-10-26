package hu.gchovanyecz.roombooking.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A validation error of a single request field, returned in the {@code errors} list of a 400 response.
 */
@Schema(description = "Egy mező hibája")
public record FieldViolation(@Schema(example = "endTime") String field,
		@Schema(example = "A foglalás legalább 15 perc legyen.") String message) {
}
