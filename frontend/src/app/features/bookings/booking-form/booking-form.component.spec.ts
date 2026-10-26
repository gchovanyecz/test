import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { BookingFormComponent } from './booking-form.component';
import { errorInterceptor } from '../../../core/http/error.interceptor';
import { BookingResponse } from '../../../core/models/booking.model';
import { RoomResponse } from '../../../core/models/room.model';
import { NotificationService } from '../../../core/notifications/notification.service';

const rooms: RoomResponse[] = [
  { id: 1, name: 'Duna', capacity: 8, location: '3. emelet' },
  { id: 2, name: 'Tisza', capacity: 4, location: 'Földszint' },
];

const createdBooking: BookingResponse = {
  id: 9,
  roomId: 2,
  roomName: 'Tisza',
  title: 'Sprint',
  startTime: new Date(2030, 0, 7, 9).toISOString(),
  endTime: new Date(2030, 0, 7, 10).toISOString(),
  createdAt: '2029-12-01T10:00:00Z',
};

describe('BookingFormComponent', () => {
  let fixture: ComponentFixture<BookingFormComponent>;
  let http: HttpTestingController;
  let notifications: NotificationService;

  const element = (): HTMLElement => fixture.nativeElement as HTMLElement;
  const control = (id: string): HTMLInputElement | HTMLSelectElement =>
    element().querySelector(`#${id}`) as HTMLInputElement | HTMLSelectElement;
  const submitButton = (): HTMLButtonElement =>
    element().querySelector('button[type="submit"]') as HTMLButtonElement;
  const errorText = (id: string): string | undefined =>
    element().querySelector(`#${id}`)?.textContent?.trim();
  const formError = (): string | undefined =>
    element().querySelector('.booking-form__error')?.textContent?.trim();

  /** Types into an input the way a user does: sets the value and fires `input`. */
  function type(id: string, value: string): void {
    const el = control(id);
    el.value = value;
    el.dispatchEvent(new Event('input'));
    fixture.detectChanges();
  }

  /** Picks a room in the select by its id (option 0 is the empty placeholder). */
  function pickRoom(roomId: number): void {
    const select = control('booking-room') as HTMLSelectElement;
    select.selectedIndex = rooms.findIndex((room) => room.id === roomId) + 1;
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();
  }

  function fillValidBooking(): void {
    pickRoom(2);
    type('booking-title', 'Sprint');
    type('booking-date', '2030-01-07');
    type('booking-start', '09:00');
    type('booking-end', '10:00');
  }

  function submit(): void {
    element().querySelector('form')?.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [BookingFormComponent],
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    notifications = TestBed.inject(NotificationService);
    fixture = TestBed.createComponent(BookingFormComponent);
    fixture.componentRef.setInput('rooms', rooms);
    fixture.componentRef.setInput('roomId', null);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('offers the rooms and starts without a room when no filter is set', () => {
    const options = Array.from((control('booking-room') as HTMLSelectElement).options).map(
      (option) => option.textContent?.trim(),
    );
    expect(options).toEqual(['Válasszon tárgyalót…', 'Duna', 'Tisza']);
    expect(fixture.componentInstance['form'].controls.roomId.value).toBeNull();
  });

  it('preselects the room of the bookings filter', () => {
    fixture.componentRef.setInput('roomId', 2);
    fixture.detectChanges();

    expect(fixture.componentInstance['form'].controls.roomId.value).toBe(2);
  });

  it('keeps the submit button disabled and explains the invalid times in Hungarian', () => {
    pickRoom(1);
    type('booking-date', '2030-01-07');
    type('booking-start', '09:00');
    type('booking-end', '09:10');

    expect(errorText('booking-end-error')).toBe(
      'Az időpontnak 15 perces időrácsra kell esnie (pl. 9:00, 9:15).',
    );
    expect(submitButton().disabled).toBe(true);
  });

  it('explains an end before the start', () => {
    pickRoom(1);
    type('booking-date', '2030-01-07');
    type('booking-start', '10:00');
    type('booking-end', '09:00');

    expect(errorText('booking-end-error')).toBe('A befejezésnek a kezdés után kell lennie.');
  });

  it('shows the title limit message under the title field', () => {
    type('booking-title', 'a'.repeat(101));

    expect(errorText('booking-title-error')).toBe('A cím legfeljebb 100 karakter lehet.');
  });

  it('does not show required-field messages before the user has touched a field', () => {
    expect(element().querySelectorAll('.field__error').length).toBe(0);
    expect(submitButton().disabled).toBe(true);
  });

  it('marks every field and sends nothing when submitted while invalid', () => {
    submit();

    expect(errorText('booking-room-error')).toBe('Válasszon tárgyalót.');
    expect(errorText('booking-date-error')).toBe('Adja meg a dátumot.');
    http.expectNone('/api/bookings');
  });

  it('enables the submit button once the form is valid', () => {
    fillValidBooking();

    expect(submitButton().disabled).toBe(false);
  });

  it('posts the booking as UTC instants and keeps the room, date and title', () => {
    fillValidBooking();
    submit();

    const req = http.expectOne('/api/bookings');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      roomId: 2,
      title: 'Sprint',
      startTime: new Date(2030, 0, 7, 9, 0).toISOString(),
      endTime: new Date(2030, 0, 7, 10, 0).toISOString(),
    });
    req.flush(createdBooking, { status: 201, statusText: 'Created' });
    fixture.detectChanges();

    expect(fixture.componentInstance['form'].controls.roomId.value).toBe(2);
    expect(fixture.componentInstance['form'].controls.title.value).toBe('Sprint');
    expect(fixture.componentInstance['form'].controls.date.value).toBe('2030-01-07');
  });

  it('sends a missing title as null', () => {
    fillValidBooking();
    type('booking-title', '   ');
    submit();

    expect(http.expectOne('/api/bookings').request.body.title).toBeNull();
  });

  it('shows a success notification, resets the times and emits booked', () => {
    const booked = vi.fn();
    fixture.componentInstance.booked.subscribe(booked);
    fillValidBooking();
    submit();

    http.expectOne('/api/bookings').flush(createdBooking, { status: 201, statusText: 'Created' });
    fixture.detectChanges();

    expect(notifications.notifications().map((item) => [item.kind, item.message])).toEqual([
      ['info', 'Foglalás rögzítve.'],
    ]);
    expect((control('booking-start') as HTMLInputElement).value).toBe('');
    expect((control('booking-end') as HTMLInputElement).value).toBe('');
    expect(errorText('booking-start-error')).toBeUndefined();
    expect(booked).toHaveBeenCalledTimes(1);
  });

  it('maps 400 field errors under the matching fields and unknown fields to the form', () => {
    fillValidBooking();
    submit();

    http.expectOne('/api/bookings').flush(
      {
        type: 'about:blank',
        title: 'Bad Request',
        status: 400,
        detail: 'A kérés érvénytelen mezőket tartalmaz.',
        instance: '/api/bookings',
        errors: [
          { field: 'startTime', message: 'A kezdés nem lehet a múltban.' },
          { field: 'title', message: 'A cím legfeljebb 100 karakter lehet.' },
          { field: 'unknownField', message: 'Ismeretlen hiba.' },
        ],
      },
      { status: 400, statusText: 'Bad Request' },
    );
    fixture.detectChanges();

    expect(errorText('booking-start-error')).toBe('A kezdés nem lehet a múltban.');
    expect(errorText('booking-title-error')).toBe('A cím legfeljebb 100 karakter lehet.');
    expect(errorText('booking-end-error')).toBeUndefined();
    expect(formError()).toBe('Ismeretlen hiba.');
    expect(notifications.notifications().length).toBe(0);
  });

  it('clears the server errors once the user edits the form again', () => {
    fillValidBooking();
    submit();
    http.expectOne('/api/bookings').flush(
      {
        type: 'about:blank',
        title: 'Bad Request',
        status: 400,
        detail: 'A kérés érvénytelen mezőket tartalmaz.',
        instance: '/api/bookings',
        errors: [{ field: 'title', message: 'A cím legfeljebb 100 karakter lehet.' }],
      },
      { status: 400, statusText: 'Bad Request' },
    );
    fixture.detectChanges();

    type('booking-title', 'Új cím');

    expect(errorText('booking-title-error')).toBeUndefined();
  });

  it('shows a 409 conflict as a form message with the conflicting time range, without a toast', () => {
    fillValidBooking();
    submit();

    http.expectOne('/api/bookings').flush(
      {
        type: 'about:blank',
        title: 'Conflict',
        status: 409,
        detail: 'A tárgyaló ebben az idősávban már foglalt.',
        instance: '/api/bookings',
        conflictingBookingId: 4,
        conflictingStartTime: new Date(2030, 0, 7, 9, 30).toISOString(),
        conflictingEndTime: new Date(2030, 0, 7, 10, 15).toISOString(),
      },
      { status: 409, statusText: 'Conflict' },
    );
    fixture.detectChanges();

    expect(formError()).toBe(
      'A tárgyaló ebben az idősávban már foglalt. Ütköző foglalás: 2030. január 7., hétfő, 09:30 – 10:15.',
    );
    expect(element().querySelector('[role="alert"]')?.textContent).toContain('már foglalt');
    expect(notifications.notifications().length).toBe(0);
  });

  it('shows a 404 unknown room as a form message without a toast', () => {
    fillValidBooking();
    submit();

    http.expectOne('/api/bookings').flush(
      {
        type: 'about:blank',
        title: 'Not Found',
        status: 404,
        detail: 'Nem létezik tárgyaló ezzel az azonosítóval: 2',
        instance: '/api/bookings',
      },
      { status: 404, statusText: 'Not Found' },
    );
    fixture.detectChanges();

    expect(formError()).toBe('Nem létezik tárgyaló ezzel az azonosítóval: 2');
    expect(notifications.notifications().length).toBe(0);
  });

  it('falls back to a generic form message for other server failures', () => {
    fillValidBooking();
    submit();

    http.expectOne('/api/bookings').flush('<html>Bad gateway</html>', {
      status: 502,
      statusText: 'Bad Gateway',
    });
    fixture.detectChanges();

    expect(formError()).toBe('Váratlan hiba történt. Próbálja meg újra később.');
    expect(notifications.notifications().length).toBe(0);
  });
});
