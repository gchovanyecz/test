package hu.gchovanyecz.roombooking.booking.service;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurable booking time rules ({@code booking.rules.*} in application.yml).
 *
 * @param minDuration shortest allowed booking
 * @param maxDuration longest allowed booking
 * @param timeGrid start and end must fall on a multiple of this step (e.g. 9:00, 9:15 for 15 minutes)
 */
@ConfigurationProperties("booking.rules")
public record BookingRulesProperties(Duration minDuration, Duration maxDuration, Duration timeGrid) {
}
