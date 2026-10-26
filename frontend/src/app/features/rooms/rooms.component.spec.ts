import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { RoomsComponent } from './rooms.component';
import { RoomResponse } from '../../core/models/room.model';

const rooms: RoomResponse[] = [
  { id: 1, name: 'Duna', capacity: 8, location: '3. emelet' },
  { id: 2, name: 'Tisza', capacity: 4, location: 'Földszint' },
];

describe('RoomsComponent', () => {
  let fixture: ComponentFixture<RoomsComponent>;
  let http: HttpTestingController;

  const element = (): HTMLElement => fixture.nativeElement as HTMLElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RoomsComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(RoomsComponent);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('shows a loading state while the rooms are requested', () => {
    http.expectOne('/api/rooms');

    expect(element().querySelector('[role="status"]')?.textContent).toContain('Betöltés');
    expect(element().querySelector('table')).toBeNull();
  });

  it('renders the rooms with name, capacity and location', () => {
    http.expectOne('/api/rooms').flush(rooms);
    fixture.detectChanges();

    const rows = Array.from(element().querySelectorAll('tbody tr')).map((row) =>
      Array.from(row.querySelectorAll('td')).map((cell) => cell.textContent?.trim()),
    );
    expect(rows).toEqual([
      ['Duna', '8 fő', '3. emelet'],
      ['Tisza', '4 fő', 'Földszint'],
    ]);
  });

  it('shows the empty state when there are no rooms', () => {
    http.expectOne('/api/rooms').flush([]);
    fixture.detectChanges();

    expect(element().querySelector('table')).toBeNull();
    expect(element().textContent).toContain('Nincs megjeleníthető tárgyaló.');
  });

  it('shows an inline error with a retry button and reloads on retry', () => {
    http.expectOne('/api/rooms').flush(
      {
        title: 'Server error',
        status: 500,
        detail: 'Hiba',
        type: 'about:blank',
        instance: '/api/rooms',
      },
      { status: 500, statusText: 'Internal Server Error' },
    );
    fixture.detectChanges();

    const alert = element().querySelector('[role="alert"]');
    expect(alert?.textContent).toContain('A tárgyalókat nem sikerült betölteni.');

    (alert?.querySelector('button') as HTMLButtonElement).click();
    http.expectOne('/api/rooms').flush(rooms);
    fixture.detectChanges();

    expect(element().querySelectorAll('tbody tr').length).toBe(2);
  });
});
