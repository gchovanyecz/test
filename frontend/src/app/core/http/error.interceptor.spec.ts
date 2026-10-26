import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpContext, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import {
  errorInterceptor,
  GENERIC_ERROR_MESSAGE,
  SKIP_ERROR_NOTIFICATION,
} from './error.interceptor';
import { NotificationService } from '../notifications/notification.service';

describe('errorInterceptor', () => {
  let http: HttpClient;
  let testing: HttpTestingController;
  let notifications: NotificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    testing = TestBed.inject(HttpTestingController);
    notifications = TestBed.inject(NotificationService);
  });

  afterEach(() => testing.verify());

  it('shows the ProblemDetail detail as an error notification and rethrows', () => {
    let failed = false;
    http.get('/api/bookings/99').subscribe({ error: () => (failed = true) });

    testing.expectOne('/api/bookings/99').flush(
      {
        type: 'about:blank',
        title: 'Not Found',
        status: 404,
        detail: 'A foglalás nem található.',
        instance: '/api/bookings/99',
      },
      { status: 404, statusText: 'Not Found' },
    );

    expect(failed).toBe(true);
    const shown = notifications.notifications();
    expect(shown.length).toBe(1);
    expect(shown[0].kind).toBe('error');
    expect(shown[0].message).toBe('A foglalás nem található.');
  });

  it('falls back to a generic message when the error body is not a ProblemDetail', () => {
    http.get('/api/rooms').subscribe({ error: () => undefined });

    testing.expectOne('/api/rooms').flush('<html>Bad gateway</html>', {
      status: 502,
      statusText: 'Bad Gateway',
    });

    expect(notifications.notifications()[0].message).toBe(GENERIC_ERROR_MESSAGE);
  });

  it('skips the notification for a request that opts out, but still rethrows the error', () => {
    let failed = false;
    http
      .get('/api/bookings', { context: new HttpContext().set(SKIP_ERROR_NOTIFICATION, true) })
      .subscribe({ error: () => (failed = true) });

    testing
      .expectOne('/api/bookings')
      .flush(
        {
          type: 'about:blank',
          title: 'Conflict',
          status: 409,
          detail: 'Ütközés',
          instance: '/api/bookings',
        },
        { status: 409, statusText: 'Conflict' },
      );

    expect(failed).toBe(true);
    expect(notifications.notifications().length).toBe(0);
  });

  it('does not add a notification for successful requests', () => {
    http.get('/api/rooms').subscribe();

    testing.expectOne('/api/rooms').flush([]);

    expect(notifications.notifications().length).toBe(0);
  });
});
