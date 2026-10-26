package hu.gchovanyecz.roombooking.booking.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import hu.gchovanyecz.roombooking.common.exception.FieldViolation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class BookingTimeRulesTests {

	private static final Instant NOW = Instant.parse("2030-01-07T08:00:00Z");

	private final BookingTimeRules rules = new BookingTimeRules(
			new BookingRulesProperties(Duration.ofMinutes(15), Duration.ofHours(8), Duration.ofMinutes(15)),
			Clock.fixed(NOW, ZoneOffset.UTC));

	@Test
	void acceptsValidBooking() {
		assertThatCode(() -> rules.validate(at("09:00"), at("10:00"))).doesNotThrowAnyException();
	}

	@Test
	void acceptsBookingStartingExactlyNow() {
		assertThatCode(() -> rules.validate(NOW, NOW.plus(Duration.ofHours(1)))).doesNotThrowAnyException();
	}

	@Test
	void acceptsMinimumAndMaximumDuration() {
		assertThatCode(() -> rules.validate(at("09:00"), at("09:15"))).doesNotThrowAnyException();
		assertThatCode(() -> rules.validate(at("09:00"), at("17:00"))).doesNotThrowAnyException();
	}

	@Test
	void rejectsStartInThePast() {
		assertThat(violations(at("07:45"), at("09:00")))
			.containsExactly(new FieldViolation("startTime", "A kezdés nem lehet a múltban."));
	}

	@Test
	void rejectsEndNotAfterStart() {
		assertThat(violations(at("10:00"), at("10:00")))
			.containsExactly(new FieldViolation("endTime", "A befejezésnek a kezdés után kell lennie."));
		assertThat(violations(at("10:00"), at("09:00")))
			.containsExactly(new FieldViolation("endTime", "A befejezésnek a kezdés után kell lennie."));
	}

	@Test
	void rejectsBookingLongerThanMaximum() {
		assertThat(violations(at("09:00"), at("17:15")))
			.containsExactly(new FieldViolation("endTime", "A foglalás legfeljebb 8 óra lehet."));
	}

	@Test
	void rejectsBookingShorterThanMinimum() {
		BookingTimeRules longMinimum = new BookingTimeRules(
				new BookingRulesProperties(Duration.ofMinutes(30), Duration.ofHours(8), Duration.ofMinutes(15)),
				Clock.fixed(NOW, ZoneOffset.UTC));

		assertThat(catchThrowableOfType(BookingValidationException.class,
				() -> longMinimum.validate(at("09:00"), at("09:15"))).getViolations())
			.containsExactly(new FieldViolation("endTime", "A foglalás legalább 30 perc legyen."));
	}

	@Test
	void rejectsTimesOffTheGrid() {
		String gridMessage = "Az időpontnak 15 perces időrácsra kell esnie (pl. 9:00, 9:15).";

		assertThat(violations(at("09:10"), at("10:00")))
			.containsExactly(new FieldViolation("startTime", gridMessage));
		assertThat(violations(at("09:00"), at("10:00").plusSeconds(30)))
			.containsExactly(new FieldViolation("endTime", gridMessage));
		assertThat(violations(at("09:00").plusNanos(1), at("10:00")))
			.containsExactly(new FieldViolation("startTime", gridMessage));
	}

	@Test
	void reportsEveryBrokenRule() {
		assertThat(violations(at("07:10"), at("07:05")))
			.extracting(FieldViolation::field)
			.containsExactly("startTime", "startTime", "endTime", "endTime");
	}

	@Test
	void acceptsLocalQuarterHoursGivenWithTimeZoneOffset() {
		Instant start = OffsetDateTime.parse("2030-01-07T11:15:00+01:00").toInstant();
		Instant end = OffsetDateTime.parse("2030-01-07T12:45:00+01:00").toInstant();

		assertThatCode(() -> rules.validate(start, end)).doesNotThrowAnyException();
	}

	private List<FieldViolation> violations(Instant start, Instant end) {
		return catchThrowableOfType(BookingValidationException.class, () -> rules.validate(start, end)).getViolations();
	}

	private static Instant at(String utcTime) {
		return Instant.parse("2030-01-07T" + utcTime + ":00Z");
	}

}
