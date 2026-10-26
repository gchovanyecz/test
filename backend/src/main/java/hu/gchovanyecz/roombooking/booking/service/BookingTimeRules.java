package hu.gchovanyecz.roombooking.booking.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import hu.gchovanyecz.roombooking.common.exception.FieldViolation;
import org.springframework.stereotype.Component;

/**
 * Checks the time rules of a new booking. Overlap with other bookings is checked separately by {@link BookingService}.
 */
@Component
public class BookingTimeRules {

	private final BookingRulesProperties rules;

	private final Clock clock;

	public BookingTimeRules(BookingRulesProperties rules, Clock clock) {
		this.rules = rules;
		this.clock = clock;
	}

	/**
	 * @throws BookingValidationException listing every broken rule
	 */
	public void validate(Instant startTime, Instant endTime) {
		List<FieldViolation> violations = new ArrayList<>();

		if (startTime.isBefore(clock.instant())) {
			violations.add(new FieldViolation("startTime", "A kezdés nem lehet a múltban."));
		}
		if (!isOnGrid(startTime)) {
			violations.add(new FieldViolation("startTime", gridMessage()));
		}
		if (!isOnGrid(endTime)) {
			violations.add(new FieldViolation("endTime", gridMessage()));
		}

		if (!endTime.isAfter(startTime)) {
			violations.add(new FieldViolation("endTime", "A befejezésnek a kezdés után kell lennie."));
		}
		else {
			Duration duration = Duration.between(startTime, endTime);
			if (duration.compareTo(rules.minDuration()) < 0) {
				violations.add(new FieldViolation("endTime",
						"A foglalás legalább " + describe(rules.minDuration()) + " legyen."));
			}
			if (duration.compareTo(rules.maxDuration()) > 0) {
				violations.add(new FieldViolation("endTime",
						"A foglalás legfeljebb " + describe(rules.maxDuration()) + " lehet."));
			}
		}

		if (!violations.isEmpty()) {
			throw new BookingValidationException(violations);
		}
	}

	/**
	 * The grid is aligned to UTC; with a 15-minute grid this matches local quarter hours in every real time zone.
	 */
	private boolean isOnGrid(Instant instant) {
		return instant.getNano() == 0 && instant.getEpochSecond() % rules.timeGrid().toSeconds() == 0;
	}

	private String gridMessage() {
		return "Az időpontnak " + rules.timeGrid().toMinutes() + " perces időrácsra kell esnie (pl. 9:00, 9:15).";
	}

	/** "8 óra" or "15 perc" for the error messages. */
	private static String describe(Duration duration) {
		return duration.toMinutesPart() == 0 ? duration.toHours() + " óra" : duration.toMinutes() + " perc";
	}

}
