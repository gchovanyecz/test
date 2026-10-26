package hu.gchovanyecz.roombooking.common.web;

import java.util.List;

import hu.gchovanyecz.roombooking.booking.service.BookingConflictException;
import hu.gchovanyecz.roombooking.booking.service.BookingValidationException;
import hu.gchovanyecz.roombooking.common.exception.FieldViolation;
import hu.gchovanyecz.roombooking.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Maps every error to an RFC 9457 {@code application/problem+json} response.
 * <p>
 * Spring MVC's own exceptions (malformed JSON, type mismatch, unknown path, ...) are handled by
 * {@link ResponseEntityExceptionHandler}; this class adds the domain exceptions and a safe fallback.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	static final String BOOKING_OVERLAP_CONSTRAINT = "booking_no_overlap";

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResourceNotFoundException.class)
	ProblemDetail handleNotFound(ResourceNotFoundException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(BookingConflictException.class)
	ProblemDetail handleBookingConflict(BookingConflictException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setProperty("conflictingBookingId", ex.getConflictingBookingId());
		problem.setProperty("conflictingStartTime", ex.getConflictingStartTime());
		problem.setProperty("conflictingEndTime", ex.getConflictingEndTime());
		return problem;
	}

	@ExceptionHandler(BookingValidationException.class)
	ProblemDetail handleBookingValidation(BookingValidationException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problem.setProperty("errors", ex.getViolations());
		return problem;
	}

	/**
	 * The service checks for overlaps first, but two concurrent requests can both pass that check;
	 * the database exclusion constraint then rejects the second one, which is still a booking conflict.
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
		if (ex.getMessage() != null && ex.getMessage().contains(BOOKING_OVERLAP_CONSTRAINT)) {
			return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "A tárgyaló ebben az idősávban már foglalt.");
		}
		return handleUnexpected(ex);
	}

	@ExceptionHandler(Exception.class)
	ProblemDetail handleUnexpected(Exception ex) {
		log.error("Unexpected error", ex);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Váratlan hiba történt.");
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ProblemDetail problem = ex.getBody();
		problem.setDetail("A kérés érvénytelen mezőket tartalmaz.");
		problem.setProperty("errors", fieldErrors(ex));
		return handleExceptionInternal(ex, problem, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status,
				"A kérés törzse nem olvasható (hibás JSON vagy érvénytelen dátumformátum).");
		return handleExceptionInternal(ex, problem, headers, status, request);
	}

	private static List<FieldViolation> fieldErrors(MethodArgumentNotValidException ex) {
		return ex.getBindingResult().getAllErrors().stream()
			.map(error -> new FieldViolation(
					error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName(),
					error.getDefaultMessage()))
			.toList();
	}

}
