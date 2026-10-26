import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  ElementRef,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { BookingApiService } from '../../core/api/booking-api.service';
import { displayTitle, formatBookingDate, formatTimeRange } from '../../core/format/booking-format';
import { formatTimestamp } from '../../core/format/timestamp-format';
import { BookingResponse } from '../../core/models/booking.model';
import { NotificationService } from '../../core/notifications/notification.service';
import { LoadStatus } from '../../shared/load-status';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminComponent {
  private readonly bookingApi = inject(BookingApiService);
  private readonly notifications = inject(NotificationService);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly destroyRef = inject(DestroyRef);

  private readonly confirmDialog =
    viewChild.required<ElementRef<HTMLDialogElement>>('confirmDialog');

  protected readonly bookings = signal<BookingResponse[]>([]);
  protected readonly status = signal<LoadStatus>('loading');
  /** Ids of the bookings whose DELETE request is in flight. Their buttons stay disabled. */
  protected readonly deletingIds = signal<ReadonlySet<number>>(new Set());
  /** The booking waiting for confirmation; `null` while the dialog is closed. */
  protected readonly pendingDelete = signal<BookingResponse | null>(null);

  protected readonly displayTitle = displayTitle;
  protected readonly formatDate = formatBookingDate;
  protected readonly formatTimes = formatTimeRange;
  protected readonly formatCreatedAt = formatTimestamp;

  private listRequest?: Subscription;

  constructor() {
    this.load();
    this.destroyRef.onDestroy(() => this.listRequest?.unsubscribe());
  }

  protected load(): void {
    this.status.set('loading');
    this.fetchBookings();
  }

  protected isDeleting(id: number): boolean {
    return this.deletingIds().has(id);
  }

  protected openDeleteDialog(booking: BookingResponse): void {
    this.pendingDelete.set(booking);
    // Render the dialog content first, so the focus of showModal() lands on the cancel button.
    this.changeDetector.detectChanges();
    this.confirmDialog().nativeElement.showModal();
  }

  protected cancelDelete(): void {
    this.confirmDialog().nativeElement.close();
  }

  /** Runs on every close (cancel button, Esc or confirm), so the pending booking is always cleared. */
  protected onDialogClosed(): void {
    this.pendingDelete.set(null);
  }

  protected confirmDelete(): void {
    const booking = this.pendingDelete();
    this.confirmDialog().nativeElement.close();
    if (booking) {
      this.delete(booking);
    }
  }

  private delete(booking: BookingResponse): void {
    this.deletingIds.update((ids) => new Set(ids).add(booking.id));
    this.bookingApi.adminDelete(booking.id).subscribe({
      complete: () => {
        this.markIdle(booking.id);
        this.notifications.info('Foglalás törölve.');
        this.bookings.update((list) => list.filter((item) => item.id !== booking.id));
        this.fetchBookings();
      },
      // The global error interceptor already shows the reason, so only the row is reset here.
      error: () => this.markIdle(booking.id),
    });
  }

  private markIdle(id: number): void {
    this.deletingIds.update((ids) => {
      const next = new Set(ids);
      next.delete(id);
      return next;
    });
  }

  /** Fetches the list without clearing the table first, so a refresh after a delete does not flicker. */
  private fetchBookings(): void {
    this.listRequest?.unsubscribe();
    this.listRequest = this.bookingApi.list().subscribe({
      next: (bookings) => {
        this.bookings.set(bookings);
        this.status.set('loaded');
      },
      error: () => this.status.set('error'),
    });
  }
}
