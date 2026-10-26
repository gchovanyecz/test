import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { RoomApiService } from './room-api.service';
import { RoomResponse } from '../models/room.model';

describe('RoomApiService', () => {
  it('lists rooms from /api/rooms', () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const service = TestBed.inject(RoomApiService);
    const http = TestBed.inject(HttpTestingController);
    const rooms: RoomResponse[] = [{ id: 1, name: 'Duna', capacity: 8, location: '3. emelet' }];

    let received: RoomResponse[] | undefined;
    service.list().subscribe((result) => (received = result));

    const req = http.expectOne('/api/rooms');
    expect(req.request.method).toBe('GET');
    req.flush(rooms);

    expect(received).toEqual(rooms);
    http.verify();
  });
});
