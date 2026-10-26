package hu.gchovanyecz.roombooking.booking.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import hu.gchovanyecz.roombooking.booking.domain.Booking;
import hu.gchovanyecz.roombooking.booking.dto.BookingResponse;
import hu.gchovanyecz.roombooking.booking.dto.CreateBookingRequest;
import hu.gchovanyecz.roombooking.booking.repository.BookingRepository;
import hu.gchovanyecz.roombooking.common.exception.FieldViolation;
import hu.gchovanyecz.roombooking.room.domain.Room;
import hu.gchovanyecz.roombooking.room.repository.RoomRepository;
import hu.gchovanyecz.roombooking.room.service.RoomNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class BookingServiceTests {

	private static final Instant NINE = Instant.parse("2030-01-07T09:00:00Z");
	private static final Instant TEN = Instant.parse("2030-01-07T10:00:00Z");

	@Mock
	private BookingRepository bookingRepository;

	@Mock
	private RoomRepository roomRepository;

	@Mock
	private BookingTimeRules timeRules;

	@InjectMocks
	private BookingService bookingService;

	@Test
	void createsBookingWhenRoomIsFree() {
		Room room = room(1L, "Budapest");
		given(roomRepository.findByIdForUpdate(1L)).willReturn(Optional.of(room));
		given(bookingRepository.findOverlapping(1L, NINE, TEN)).willReturn(List.of());
		given(bookingRepository.saveAndFlush(any())).willAnswer(invocation -> withId(invocation.getArgument(0), 5L));

		BookingResponse response = bookingService.create(new CreateBookingRequest(1L, "  Sprint planning ", NINE, TEN));

		assertThat(response.id()).isEqualTo(5L);
		assertThat(response.roomId()).isEqualTo(1L);
		assertThat(response.roomName()).isEqualTo("Budapest");
		assertThat(response.title()).isEqualTo("Sprint planning");
		assertThat(response.startTime()).isEqualTo(NINE);
		assertThat(response.endTime()).isEqualTo(TEN);
	}

	@Test
	void storesBlankTitleAsNull() {
		given(roomRepository.findByIdForUpdate(1L)).willReturn(Optional.of(room(1L, "Budapest")));
		given(bookingRepository.findOverlapping(1L, NINE, TEN)).willReturn(List.of());
		given(bookingRepository.saveAndFlush(any())).willAnswer(invocation -> withId(invocation.getArgument(0), 5L));

		bookingService.create(new CreateBookingRequest(1L, "   ", NINE, TEN));

		ArgumentCaptor<Booking> saved = ArgumentCaptor.forClass(Booking.class);
		verify(bookingRepository).saveAndFlush(saved.capture());
		assertThat(saved.getValue().getTitle()).isNull();
	}

	@Test
	void rejectsOverlappingBookingWithTheEarliestConflict() {
		given(roomRepository.findByIdForUpdate(1L)).willReturn(Optional.of(room(1L, "Budapest")));
		Booking existing = withId(new Booking(room(1L, "Budapest"), null, NINE.minusSeconds(1800), NINE.plusSeconds(1800)), 3L);
		given(bookingRepository.findOverlapping(1L, NINE, TEN)).willReturn(List.of(existing));

		assertThatThrownBy(() -> bookingService.create(new CreateBookingRequest(1L, null, NINE, TEN)))
			.isInstanceOfSatisfying(BookingConflictException.class, conflict -> {
				assertThat(conflict.getConflictingBookingId()).isEqualTo(3L);
				assertThat(conflict.getConflictingStartTime()).isEqualTo(existing.getStartTime());
				assertThat(conflict.getConflictingEndTime()).isEqualTo(existing.getEndTime());
			});
		verify(bookingRepository, never()).saveAndFlush(any());
	}

	@Test
	void rejectsUnknownRoom() {
		given(roomRepository.findByIdForUpdate(99L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> bookingService.create(new CreateBookingRequest(99L, null, NINE, TEN)))
			.isInstanceOf(RoomNotFoundException.class);
		verify(bookingRepository, never()).saveAndFlush(any());
	}

	@Test
	void checksTimeRulesBeforeTouchingTheDatabase() {
		willThrow(new BookingValidationException(List.of(new FieldViolation("endTime", "hiba"))))
			.given(timeRules).validate(TEN, NINE);

		assertThatThrownBy(() -> bookingService.create(new CreateBookingRequest(1L, null, TEN, NINE)))
			.isInstanceOf(BookingValidationException.class);
		verifyNoInteractions(roomRepository, bookingRepository);
	}

	private static Room room(long id, String name) {
		Room room = BeanUtils.instantiateClass(Room.class);
		ReflectionTestUtils.setField(room, "id", id);
		ReflectionTestUtils.setField(room, "name", name);
		return room;
	}

	private static Booking withId(Booking booking, long id) {
		ReflectionTestUtils.setField(booking, "id", id);
		return booking;
	}

}
