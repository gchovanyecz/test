import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { BookingsComponent } from './bookings.component';
import { BookingResponse } from '../../core/models/booking.model';
import { RoomResponse } from '../../core/models/room.model';

const rooms: RoomResponse[] = [
  { id: 1, name: 'Duna', capacity: 8, location: '3. emelet' },
  { id: 2, name: 'Tisza', capacity: 4, location: 'Földszint' },
];

/** Builds an instant from local wall-clock time, so the expected text does not depend on the TZ. */
function localIso(year: number, month: number, day: number, hour: number, minute = 0): string {
  return new Date(year, month, day, hour, minute).toISOString();
}

const bookings: BookingResponse[] = [
  {
    id: 7,
    roomId: 1,
    roomName: 'Duna',
    title: 'Heti megbeszélés',
    startTime: localIso(2026, 10, 2, 9),
    endTime: localIso(2026, 10, 2, 10, 30),
    createdAt: '2026-10-26T12:00:00Z',
  },
  {
    id: 8,
    roomId: 2,
    roomName: 'Tisza',
    title: null,
    startTime: localIso(2026, 10, 2, 13),
    endTime: localIso(2026, 10, 2, 14),
    createdAt: '2026-10-26T12:05:00Z',
  },
];

describe('BookingsComponent', () => {
  let fixture: ComponentFixture<BookingsComponent>;
  let http: HttpTestingController;

  const element = (): HTMLElement => fixture.nativeElement as HTMLElement;
  const select = (): HTMLSelectElement =>
    element().querySelector('#room-filter') as HTMLSelectElement;

  /** Flushes the initial room list, which fills the filter options. */
  function flushRooms(): void {
    http.expectOne('/api/rooms').flush(rooms);
    fixture.detectChanges();
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [BookingsComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(BookingsComponent);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('loads all bookings and the room options on start', () => {
    const bookingsRequest = http.expectOne((req) => req.url === '/api/bookings');
    expect(bookingsRequest.request.params.keys()).toEqual([]);
    bookingsRequest.flush(bookings);
    flushRooms();

    const options = Array.from(select().options).map((option) => option.textContent?.trim());
    expect(options).toEqual(['Összes tárgyaló', 'Duna', 'Tisza']);
    expect(select().value).toBe('');
  });

  it('renders title, room, date and time of each booking', () => {
    http.expectOne((req) => req.url === '/api/bookings').flush(bookings);
    flushRooms();

    const rows = Array.from(element().querySelectorAll('tbody tr')).map((row) =>
      Array.from(row.querySelectorAll('td')).map((cell) => cell.textContent?.trim()),
    );
    expect(rows).toEqual([
      ['Heti megbeszélés', 'Duna', '2026. november 2., hétfő', '09:00 – 10:30'],
      ['(nincs cím)', 'Tisza', '2026. november 2., hétfő', '13:00 – 14:00'],
    ]);
  });

  it('marks the placeholder title so it can be styled differently', () => {
    http.expectOne((req) => req.url === '/api/bookings').flush(bookings);
    flushRooms();

    const titles = element().querySelectorAll('tbody tr td:first-child');
    expect(titles[0].classList.contains('table__placeholder')).toBe(false);
    expect(titles[1].classList.contains('table__placeholder')).toBe(true);
  });

  it('shows the empty state when there are no bookings', () => {
    http.expectOne((req) => req.url === '/api/bookings').flush([]);
    flushRooms();

    expect(element().querySelector('table')).toBeNull();
    expect(element().textContent).toContain('Nincs foglalás');
  });

  it('reloads the bookings with roomId when the room filter changes', async () => {
    http.expectOne((req) => req.url === '/api/bookings').flush(bookings);
    flushRooms();

    select().value = '2';
    select().dispatchEvent(new Event('change'));
    await fixture.whenStable();
    fixture.detectChanges();

    const filtered = http.expectOne((req) => req.url === '/api/bookings');
    expect(filtered.request.params.get('roomId')).toBe('2');
    filtered.flush([bookings[1]]);
    fixture.detectChanges();

    expect(element().querySelectorAll('tbody tr').length).toBe(1);
    expect(element().querySelector('tbody td')?.textContent).toContain('(nincs cím)');
  });

  it('drops the roomId parameter when the filter is set back to all rooms', async () => {
    http.expectOne((req) => req.url === '/api/bookings').flush(bookings);
    flushRooms();

    select().value = '1';
    select().dispatchEvent(new Event('change'));
    await fixture.whenStable();
    http.expectOne((req) => req.url === '/api/bookings').flush([bookings[0]]);

    select().value = '';
    select().dispatchEvent(new Event('change'));
    await fixture.whenStable();
    const all = http.expectOne((req) => req.url === '/api/bookings');
    expect(all.request.params.has('roomId')).toBe(false);
    all.flush(bookings);
  });

  it('shows an inline error with a retry button when the bookings fail to load', () => {
    http
      .expectOne((req) => req.url === '/api/bookings')
      .flush(
        {
          title: 'Server error',
          status: 500,
          detail: 'Hiba',
          type: 'about:blank',
          instance: '/api/bookings',
        },
        { status: 500, statusText: 'Internal Server Error' },
      );
    flushRooms();

    const alert = element().querySelector('[role="alert"]');
    expect(alert?.textContent).toContain('A foglalásokat nem sikerült betölteni.');

    (alert?.querySelector('button') as HTMLButtonElement).click();
    http.expectOne((req) => req.url === '/api/bookings').flush(bookings);
    fixture.detectChanges();

    expect(element().querySelectorAll('tbody tr').length).toBe(2);
  });
});
