import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  effect,
  inject,
  input,
  output,
  signal,
  untracked,
} from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { finalize, map } from 'rxjs';
import { BookingApiService } from '../../../core/api/booking-api.service';
import { errorMessageFor, GENERIC_ERROR_MESSAGE } from '../../../core/http/error.interceptor';
import { formatBookingDate, formatTimeRange } from '../../../core/format/booking-format';
import { toIsoInstant } from '../../../core/format/local-date-time';
import { CreateBookingRequest } from '../../../core/models/booking.model';
import { isProblemDetail } from '../../../core/models/problem-detail.model';
import { RoomResponse } from '../../../core/models/room.model';
import { NotificationService } from '../../../core/notifications/notification.service';
import { BookingField, BookingFieldErrors, validateBookingForm } from './booking-form.validation';

/** Fields the backend reports in 400 responses. `date` is a client-only field. */
const SERVER_FIELDS: readonly BookingField[] = ['roomId', 'title', 'startTime', 'endTime'];

function isServerField(field: string): field is BookingField {
  return (SERVER_FIELDS as readonly string[]).includes(field);
}

/** Reads a string property of the ProblemDetail body, if present (e.g. `conflictingStartTime`). */
function problemString(body: unknown, key: string): string | null {
  if (typeof body !== 'object' || body === null) {
    return null;
  }
  const value = (body as Record<string, unknown>)[key];
  return typeof value === 'string' ? value : null;
}

@Component({
  selector: 'app-booking-form',
  imports: [ReactiveFormsModule],
  templateUrl: './booking-form.component.html',
  styleUrl: './booking-form.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BookingFormComponent {
  private readonly bookingApi = inject(BookingApiService);
  private readonly notifications = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);

  /** Rooms for the select, loaded by the parent page. */
  readonly rooms = input.required<RoomResponse[]>();
  /** Room to preselect, e.g. the room filter of the bookings page. `null` keeps the current choice. */
  readonly roomId = input<number | null>(null);

  /** Emitted after a booking was saved, so the parent can reload its list. */
  readonly booked = output<void>();

  protected readonly form = new FormGroup({
    roomId: new FormControl<number | null>(null),
    title: new FormControl('', { nonNullable: true }),
    date: new FormControl('', { nonNullable: true }),
    startTime: new FormControl('', { nonNullable: true }),
    endTime: new FormControl('', { nonNullable: true }),
  });

  protected readonly submitting = signal(false);
  /** Errors returned by the backend for the last submit. Cleared when the user edits the form. */
  protected readonly serverErrors = signal<BookingFieldErrors>({});
  /** Form-level error (409, 404, unknown 400 fields, other failures). */
  protected readonly formError = signal<string | null>(null);

  private readonly value = toSignal(
    this.form.valueChanges.pipe(map(() => this.form.getRawValue())),
    { initialValue: this.form.getRawValue() },
  );
  private readonly clientErrors = computed(() => validateBookingForm(this.value(), new Date()));
  protected readonly canSubmit = computed(() => Object.keys(this.clientErrors()).length === 0);

  constructor() {
    this.form.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.serverErrors.set({});
      this.formError.set(null);
    });

    // Prefill (and follow) the room filter of the bookings page; a null filter keeps the choice.
    effect(() => {
      const roomId = this.roomId();
      if (roomId !== null) {
        untracked(() => this.form.controls.roomId.setValue(roomId));
      }
    });
  }

  /** The message to show under a field: backend errors always, client errors once the field was used. */
  protected errorFor(field: BookingField): string | null {
    const serverError = this.serverErrors()[field];
    if (serverError) {
      return serverError;
    }
    const control = this.form.controls[field];
    if (!control.touched && !control.dirty) {
      return null;
    }
    return this.clientErrors()[field] ?? null;
  }

  protected submit(): void {
    if (this.submitting()) {
      return;
    }
    this.form.markAllAsTouched();
    const value = this.form.getRawValue();
    const errors = validateBookingForm(value, new Date());
    if (value.roomId === null || Object.keys(errors).length > 0) {
      return;
    }

    const startTime = toIsoInstant(value.date, value.startTime);
    const endTime = toIsoInstant(value.date, value.endTime);
    if (!startTime || !endTime) {
      return;
    }

    const title = value.title.trim();
    const request: CreateBookingRequest = {
      roomId: value.roomId,
      title: title === '' ? null : title,
      startTime,
      endTime,
    };

    this.submitting.set(true);
    this.bookingApi
      .create(request)
      .pipe(
        finalize(() => this.submitting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => this.onBooked(),
        error: (error: unknown) => this.showServerError(error),
      });
  }

  private onBooked(): void {
    this.notifications.info('Foglalás rögzítve.');
    // The room, date and title stay, so the next booking for the same slot is quick to enter.
    for (const field of ['startTime', 'endTime'] as const) {
      this.form.controls[field].reset('');
      this.form.controls[field].markAsUntouched();
      this.form.controls[field].markAsPristine();
    }
    this.booked.emit();
  }

  private showServerError(error: unknown): void {
    if (!(error instanceof HttpErrorResponse)) {
      this.formError.set(GENERIC_ERROR_MESSAGE);
      return;
    }

    if (error.status === 400) {
      const body: unknown = error.error;
      const fieldErrors: BookingFieldErrors = {};
      const general: string[] = [];
      const problemErrors = isProblemDetail(body) ? (body.errors ?? []) : [];
      for (const item of problemErrors) {
        if (isServerField(item.field)) {
          fieldErrors[item.field] ??= item.message;
        } else {
          general.push(item.message);
        }
      }
      if (Object.keys(fieldErrors).length === 0 && general.length === 0) {
        general.push(errorMessageFor(error));
      }
      this.serverErrors.set(fieldErrors);
      this.formError.set(general.length > 0 ? general.join(' ') : null);
      return;
    }

    if (error.status === 409) {
      this.formError.set(this.conflictMessage(error));
      return;
    }

    this.formError.set(errorMessageFor(error));
  }

  /** The 409 detail plus the conflicting booking's time, e.g. "… Ütköző foglalás: 2030. január 7., hétfő, 09:00 – 10:30." */
  private conflictMessage(error: HttpErrorResponse): string {
    const detail = errorMessageFor(error);
    const start = problemString(error.error, 'conflictingStartTime');
    const end = problemString(error.error, 'conflictingEndTime');
    if (!start || !end) {
      return detail;
    }
    return `${detail} Ütköző foglalás: ${formatBookingDate(start)}, ${formatTimeRange(start, end)}.`;
  }
}
