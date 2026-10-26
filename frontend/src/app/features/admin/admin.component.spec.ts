import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AdminComponent } from './admin.component';
import { BookingResponse } from '../../core/models/booking.model';
import { NotificationService } from '../../core/notifications/notification.service';

/**
 * jsdom does not implement <dialog>.showModal()/close(). This minimal stand-in sets the `open`
 * attribute and fires `close` the way a browser does, so the component code runs unchanged.
 */
function installDialogPolyfill(): void {
  if (typeof document.createElement('dialog').showModal === 'function') {
    return;
  }
  const proto = HTMLElement.prototype as unknown as {
    showModal(): void;
    close(): void;
  };
  proto.showModal = function (this: HTMLElement): void {
    this.setAttribute('open', '');
  };
  proto.close = function (this: HTMLElement): void {
    if (!this.hasAttribute('open')) {
      return;
    }
    this.removeAttribute('open');
    this.dispatchEvent(new Event('close'));
  };
}

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
    createdAt: localIso(2026, 9, 26, 12, 0),
  },
  {
    id: 8,
    roomId: 2,
    roomName: 'Tisza',
    title: null,
    startTime: localIso(2026, 10, 2, 13),
    endTime: localIso(2026, 10, 2, 14),
    createdAt: localIso(2026, 9, 26, 12, 5),
  },
];

const problemDetail = {
  type: 'about:blank',
  title: 'Not Found',
  status: 404,
  detail: 'A foglalás nem található.',
  instance: '/api/admin/bookings/7',
};

describe('AdminComponent', () => {
  let fixture: ComponentFixture<AdminComponent>;
  let http: HttpTestingController;
  let notifications: NotificationService;

  const element = (): HTMLElement => fixture.nativeElement as HTMLElement;
  const dialog = (): HTMLDialogElement => element().querySelector('dialog') as HTMLDialogElement;
  const rows = (): HTMLTableRowElement[] =>
    Array.from(element().querySelectorAll<HTMLTableRowElement>('tbody tr'));
  const rowButton = (index: number): HTMLButtonElement =>
    rows()[index].querySelector('button') as HTMLButtonElement;
  const dialogButton = (label: string): HTMLButtonElement =>
    Array.from(dialog().querySelectorAll<HTMLButtonElement>('button')).find(
      (button) => button.textContent?.trim() === label,
    ) as HTMLButtonElement;

  /** Flushes the initial list request and renders the table. */
  function flushList(list: BookingResponse[] = bookings): void {
    http.expectOne((req) => req.url === '/api/bookings').flush(list);
    fixture.detectChanges();
  }

  /** Opens the confirm dialog for the given row and renders it. */
  function openDialogFor(index: number): void {
    rowButton(index).click();
    fixture.detectChanges();
  }

  beforeAll(() => installDialogPolyfill());

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AdminComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    notifications = TestBed.inject(NotificationService);
    fixture = TestBed.createComponent(AdminComponent);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('shows the admin heading and notice', () => {
    flushList();

    expect(element().querySelector('h1')?.textContent?.trim()).toBe('Adminisztráció');
    expect(element().querySelector('.notice')?.textContent).toContain('adminisztrációs felület');
  });

  it('lists all bookings without a roomId filter', () => {
    const request = http.expectOne((req) => req.url === '/api/bookings');
    expect(request.request.params.keys()).toEqual([]);
    request.flush(bookings);
    fixture.detectChanges();

    expect(rows().length).toBe(2);
  });

  it('renders title, room, date, time range and creation time of each booking', () => {
    flushList();

    const cells = rows().map((row) =>
      Array.from(row.querySelectorAll('td'))
        .slice(0, 5)
        .map((cell) => cell.textContent?.trim()),
    );
    expect(cells).toEqual([
      [
        'Heti megbeszélés',
        'Duna',
        '2026. november 2., hétfő',
        '09:00 – 10:30',
        '2026. 10. 26. 12:00',
      ],
      ['(nincs cím)', 'Tisza', '2026. november 2., hétfő', '13:00 – 14:00', '2026. 10. 26. 12:05'],
    ]);
  });

  it('shows the empty state when there are no bookings', () => {
    flushList([]);

    expect(element().querySelector('table')).toBeNull();
    expect(element().textContent).toContain('Nincs foglalás');
  });

  it('shows an error with a retry button when the list fails to load', () => {
    http
      .expectOne((req) => req.url === '/api/bookings')
      .flush(problemDetail, { status: 500, statusText: 'Internal Server Error' });
    fixture.detectChanges();

    const alert = element().querySelector('[role="alert"]');
    expect(alert?.textContent).toContain('A foglalásokat nem sikerült betölteni.');

    (alert?.querySelector('button') as HTMLButtonElement).click();
    flushList();

    expect(rows().length).toBe(2);
  });

  it('opens a confirm dialog that shows the selected booking', () => {
    flushList();

    openDialogFor(0);

    expect(dialog().hasAttribute('open')).toBe(true);
    expect(dialog().textContent).toContain('Heti megbeszélés');
    expect(dialog().textContent).toContain('Duna');
    expect(dialog().textContent).not.toContain('Tisza');
    expect(dialog().getAttribute('aria-labelledby')).toBe('delete-dialog-title');
  });

  it('closes the dialog on cancel without calling the API', () => {
    flushList();
    openDialogFor(0);

    dialogButton('Mégse').click();
    fixture.detectChanges();

    expect(dialog().hasAttribute('open')).toBe(false);
    expect(rows().length).toBe(2);
    // afterEach(http.verify) fails if a DELETE was sent.
  });

  it('deletes through the admin endpoint, shows a notification and reloads the list', () => {
    flushList();
    openDialogFor(0);

    dialogButton('Törlés').click();
    fixture.detectChanges();
    expect(dialog().hasAttribute('open')).toBe(false);

    http.expectOne('/api/admin/bookings/7').flush(null, { status: 204, statusText: 'No Content' });
    fixture.detectChanges();
    expect(rows().length).toBe(1);

    const reload = http.expectOne((req) => req.url === '/api/bookings');
    reload.flush([bookings[1]]);
    fixture.detectChanges();

    expect(rows().length).toBe(1);
    expect(rows()[0].querySelector('td')?.textContent).toContain('(nincs cím)');
    expect(notifications.notifications().map((item) => [item.kind, item.message])).toEqual([
      ['info', 'Foglalás törölve.'],
    ]);
  });

  it('disables the buttons of the row being deleted until the request finishes', () => {
    flushList();
    openDialogFor(0);

    dialogButton('Törlés').click();
    fixture.detectChanges();

    expect(rowButton(0).disabled).toBe(true);
    expect(rowButton(1).disabled).toBe(false);

    http.expectOne('/api/admin/bookings/7').flush(null, { status: 204, statusText: 'No Content' });
    http.expectOne((req) => req.url === '/api/bookings').flush([bookings[1]]);
    fixture.detectChanges();
    expect(rowButton(0).disabled).toBe(false);
  });

  it('keeps the row and re-enables its buttons when the delete fails', () => {
    flushList();
    openDialogFor(0);

    dialogButton('Törlés').click();
    fixture.detectChanges();

    http.expectOne('/api/admin/bookings/7').flush(problemDetail, {
      status: 404,
      statusText: 'Not Found',
    });
    fixture.detectChanges();

    expect(rows().length).toBe(2);
    expect(rowButton(0).disabled).toBe(false);
    // The error toast comes from the global interceptor, so no notification is added here.
    expect(notifications.notifications().length).toBe(0);
    // No list reload after a failed delete.
    http.expectNone((req) => req.url === '/api/bookings');
  });
});
