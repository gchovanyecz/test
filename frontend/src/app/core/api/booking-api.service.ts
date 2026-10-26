import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { SKIP_ERROR_NOTIFICATION } from '../http/error.interceptor';
import { BookingQuery, BookingResponse, CreateBookingRequest } from '../models/booking.model';

@Injectable({ providedIn: 'root' })
export class BookingApiService {
  private readonly http = inject(HttpClient);

  list(query: BookingQuery = {}): Observable<BookingResponse[]> {
    let params = new HttpParams();
    if (query.roomId !== undefined) {
      params = params.set('roomId', query.roomId);
    }
    if (query.from !== undefined) {
      params = params.set('from', query.from);
    }
    if (query.to !== undefined) {
      params = params.set('to', query.to);
    }
    return this.http.get<BookingResponse[]>('/api/bookings', { params });
  }

  get(id: number): Observable<BookingResponse> {
    return this.http.get<BookingResponse>(`/api/bookings/${id}`);
  }

  /**
   * The only caller is the booking form, which shows failures inline, so the global error
   * notification is skipped to avoid showing the same error twice.
   */
  create(request: CreateBookingRequest): Observable<BookingResponse> {
    return this.http.post<BookingResponse>('/api/bookings', request, {
      context: new HttpContext().set(SKIP_ERROR_NOTIFICATION, true),
    });
  }

  adminDelete(id: number): Observable<void> {
    return this.http.delete<void>(`/api/admin/bookings/${id}`);
  }
}
