import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { RoomResponse } from '../models/room.model';

@Injectable({ providedIn: 'root' })
export class RoomApiService {
  private readonly http = inject(HttpClient);

  list(): Observable<RoomResponse[]> {
    return this.http.get<RoomResponse[]>('/api/rooms');
  }
}
