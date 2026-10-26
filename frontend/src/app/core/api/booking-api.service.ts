import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
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

  create(request: CreateBookingRequest): Observable<BookingResponse> {
    return this.http.post<BookingResponse>('/api/bookings', request);
  }

  adminDelete(id: number): Observable<void> {
    return this.http.delete<void>(`/api/admin/bookings/${id}`);
  }
}
