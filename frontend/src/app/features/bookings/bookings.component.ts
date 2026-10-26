import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { distinctUntilChanged, map, Subscription } from 'rxjs';
import { BookingApiService } from '../../core/api/booking-api.service';
import { RoomApiService } from '../../core/api/room-api.service';
import { displayTitle, formatBookingDate, formatTimeRange } from '../../core/format/booking-format';
import { BookingResponse } from '../../core/models/booking.model';
import { RoomResponse } from '../../core/models/room.model';
import { LoadStatus } from '../../shared/load-status';

/** Parses the `roomId` filter; anything that is not a positive integer means "all rooms". */
function parseRoomId(value: string | null): number | null {
  if (!value) {
    return null;
  }
  const id = Number(value);
  return Number.isInteger(id) && id > 0 ? id : null;
}

@Component({
  selector: 'app-bookings',
  templateUrl: './bookings.component.html',
  styleUrl: './bookings.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BookingsComponent {
  private readonly bookingApi = inject(BookingApiService);
  private readonly roomApi = inject(RoomApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly rooms = signal<RoomResponse[]>([]);
  protected readonly roomsStatus = signal<LoadStatus>('loading');
  protected readonly bookings = signal<BookingResponse[]>([]);
  protected readonly bookingsStatus = signal<LoadStatus>('loading');
  /** `null` means all rooms. The value comes from the `roomId` query parameter. */
  protected readonly selectedRoomId = signal<number | null>(null);

  protected readonly displayTitle = displayTitle;
  protected readonly formatDate = formatBookingDate;
  protected readonly formatTimes = formatTimeRange;

  private bookingsRequest?: Subscription;

  constructor() {
    this.loadRooms();

    // The URL is the single source of truth for the filter: changing the select navigates,
    // and the bookings are reloaded from the emitted query parameter (also on back/forward).
    this.route.queryParamMap
      .pipe(
        map((params) => parseRoomId(params.get('roomId'))),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((roomId) => {
        this.selectedRoomId.set(roomId);
        this.loadBookings();
      });

    this.destroyRef.onDestroy(() => this.bookingsRequest?.unsubscribe());
  }

  protected onRoomFilterChange(value: string): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { roomId: parseRoomId(value) },
      queryParamsHandling: 'merge',
    });
  }

  protected loadRooms(): void {
    this.roomsStatus.set('loading');
    this.roomApi
      .list()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (rooms) => {
          this.rooms.set(rooms);
          this.roomsStatus.set('loaded');
        },
        error: () => this.roomsStatus.set('error'),
      });
  }

  protected loadBookings(): void {
    this.bookingsRequest?.unsubscribe();
    const roomId = this.selectedRoomId();
    this.bookingsStatus.set('loading');
    this.bookingsRequest = this.bookingApi.list(roomId === null ? {} : { roomId }).subscribe({
      next: (bookings) => {
        this.bookings.set(bookings);
        this.bookingsStatus.set('loaded');
      },
      error: () => this.bookingsStatus.set('error'),
    });
  }
}
