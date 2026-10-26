import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { BookingApiService } from './booking-api.service';
import { SKIP_ERROR_NOTIFICATION } from '../http/error.interceptor';
import { BookingResponse, CreateBookingRequest } from '../models/booking.model';

const booking: BookingResponse = {
  id: 7,
  roomId: 2,
  roomName: 'Duna',
  title: 'Heti megbeszélés',
  startTime: '2026-11-02T09:00:00Z',
  endTime: '2026-11-02T10:00:00Z',
  createdAt: '2026-10-26T12:00:00Z',
};

describe('BookingApiService', () => {
  let service: BookingApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(BookingApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('lists bookings without query params when no filter is given', () => {
    service.list().subscribe((result) => expect(result).toEqual([booking]));

    const req = http.expectOne((r) => r.url === '/api/bookings');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.keys()).toEqual([]);
    req.flush([booking]);
  });

  it('passes roomId, from and to as query params', () => {
    service
      .list({ roomId: 2, from: '2026-11-02T00:00:00Z', to: '2026-11-03T00:00:00Z' })
      .subscribe();

    const req = http.expectOne((r) => r.url === '/api/bookings');
    expect(req.request.params.get('roomId')).toBe('2');
    expect(req.request.params.get('from')).toBe('2026-11-02T00:00:00Z');
    expect(req.request.params.get('to')).toBe('2026-11-03T00:00:00Z');
    req.flush([]);
  });

  it('gets a single booking by id', () => {
    service.get(7).subscribe((result) => expect(result).toEqual(booking));

    const req = http.expectOne('/api/bookings/7');
    expect(req.request.method).toBe('GET');
    req.flush(booking);
  });

  it('posts a new booking and returns the created resource', () => {
    const body: CreateBookingRequest = {
      roomId: 2,
      title: 'Heti megbeszélés',
      startTime: '2026-11-02T09:00:00Z',
      endTime: '2026-11-02T10:00:00Z',
    };
    service.create(body).subscribe((result) => expect(result).toEqual(booking));

    const req = http.expectOne('/api/bookings');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    expect(req.request.context.get(SKIP_ERROR_NOTIFICATION)).toBe(true);
    req.flush(booking, { status: 201, statusText: 'Created' });
  });

  it('deletes a booking through the admin endpoint', () => {
    service.adminDelete(7).subscribe();

    const req = http.expectOne('/api/admin/bookings/7');
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });
  });
});
