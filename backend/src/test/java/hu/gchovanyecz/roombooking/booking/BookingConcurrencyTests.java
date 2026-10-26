package hu.gchovanyecz.roombooking.booking;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import hu.gchovanyecz.roombooking.TestcontainersConfiguration;
import hu.gchovanyecz.roombooking.booking.repository.BookingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Several clients book the same slot at the same moment through the real HTTP stack. Exactly one must succeed; every
 * other request must get 409, whether the service check or the database exclusion constraint caught it.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class BookingConcurrencyTests {

	private static final int CLIENTS = 8;

	private static final String BODY = """
			{ "roomId": 1, "startTime": "2099-01-05T10:00:00Z", "endTime": "2099-01-05T11:00:00Z" }
			""";

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private BookingRepository bookingRepository;

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@AfterEach
	void deleteBookings() {
		bookingRepository.deleteAll();
	}

	@RepeatedTest(3)
	void exactlyOneOfConcurrentBookingsForTheSameSlotSucceeds() throws Exception {
		CountDownLatch start = new CountDownLatch(1);
		List<Future<Integer>> statuses = new ArrayList<>();

		try (ExecutorService executor = Executors.newFixedThreadPool(CLIENTS)) {
			for (int i = 0; i < CLIENTS; i++) {
				statuses.add(executor.submit(() -> {
					start.await();
					return postBooking();
				}));
			}
			start.countDown();

			List<Integer> results = new ArrayList<>();
			for (Future<Integer> status : statuses) {
				results.add(status.get());
			}

			assertThat(results).containsOnlyOnce(201);
			assertThat(results).filteredOn(status -> status != 201).hasSize(CLIENTS - 1).containsOnly(409);
		}
		assertThat(bookingRepository.count()).isEqualTo(1);
	}

	private int postBooking() throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/bookings"))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(BODY))
			.build();
		return httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
	}

}
