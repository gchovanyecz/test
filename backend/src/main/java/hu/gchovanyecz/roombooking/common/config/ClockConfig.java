package hu.gchovanyecz.roombooking.common.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The application reads "now" from this bean so that time-dependent rules can be tested with a fixed clock.
 */
@Configuration(proxyBeanMethods = false)
class ClockConfig {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

}
