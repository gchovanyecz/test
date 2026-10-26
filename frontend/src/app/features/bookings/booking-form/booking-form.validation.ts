import { parseLocalDateTime } from '../../../core/format/local-date-time';

/**
 * Client-side mirror of the backend booking rules (see BookingTimeRules and CreateBookingRequest).
 * The backend stays the authority; these checks give immediate, field-level feedback.
 */
export const MIN_DURATION_MINUTES = 15;
export const MAX_DURATION_MINUTES = 8 * 60;
export const TIME_GRID_MINUTES = 15;
export const TITLE_MAX_LENGTH = 100;

export interface BookingFormValue {
  roomId: number | null;
  title: string;
  /** `yyyy-mm-dd` from `<input type="date">`, empty when not set */
  date: string;
  /** `HH:mm` from `<input type="time">`, empty when not set */
  startTime: string;
  endTime: string;
}

export type BookingField = keyof BookingFormValue;

export type BookingFieldErrors = Partial<Record<BookingField, string>>;

export const MESSAGES = {
  roomRequired: 'Válasszon tárgyalót.',
  titleTooLong: `A cím legfeljebb ${TITLE_MAX_LENGTH} karakter lehet.`,
  dateRequired: 'Adja meg a dátumot.',
  startRequired: 'Adja meg a kezdési időpontot.',
  endRequired: 'Adja meg a befejezési időpontot.',
  nonexistentTime: 'Ez az időpont nem létezik a helyi időzónában (óraátállás).',
  startInPast: 'A kezdés nem lehet a múltban.',
  grid: `Az időpontnak ${TIME_GRID_MINUTES} perces időrácsra kell esnie (pl. 9:00, 9:15).`,
  endBeforeStart: 'A befejezésnek a kezdés után kell lennie.',
  tooShort: `A foglalás legalább ${MIN_DURATION_MINUTES} perc legyen.`,
  tooLong: `A foglalás legfeljebb ${MAX_DURATION_MINUTES / 60} óra lehet.`,
} as const;

function isOnGrid(date: Date): boolean {
  return date.getMinutes() % TIME_GRID_MINUTES === 0;
}

/**
 * Returns the message for every invalid field. An empty object means the form can be submitted.
 * `now` is passed in so that the "not in the past" rule can be tested.
 */
export function validateBookingForm(value: BookingFormValue, now: Date): BookingFieldErrors {
  const errors: BookingFieldErrors = {};

  if (value.roomId === null) {
    errors.roomId = MESSAGES.roomRequired;
  }
  if (value.title.length > TITLE_MAX_LENGTH) {
    errors.title = MESSAGES.titleTooLong;
  }
  if (!value.date) {
    errors.date = MESSAGES.dateRequired;
  }
  if (!value.startTime) {
    errors.startTime = MESSAGES.startRequired;
  }
  if (!value.endTime) {
    errors.endTime = MESSAGES.endRequired;
  }
  if (!value.date || !value.startTime || !value.endTime) {
    return errors;
  }

  // Both times share the date of the form, so a booking cannot run past midnight.
  const start = parseLocalDateTime(value.date, value.startTime);
  const end = parseLocalDateTime(value.date, value.endTime);

  if (!start) {
    errors.startTime = MESSAGES.nonexistentTime;
  } else if (!isOnGrid(start)) {
    errors.startTime = MESSAGES.grid;
  } else if (start.getTime() < now.getTime()) {
    errors.startTime = MESSAGES.startInPast;
  }

  if (!end) {
    errors.endTime = MESSAGES.nonexistentTime;
  } else if (!isOnGrid(end)) {
    errors.endTime = MESSAGES.grid;
  } else if (start && end.getTime() <= start.getTime()) {
    errors.endTime = MESSAGES.endBeforeStart;
  } else if (start) {
    const minutes = (end.getTime() - start.getTime()) / 60_000;
    if (minutes < MIN_DURATION_MINUTES) {
      errors.endTime = MESSAGES.tooShort;
    } else if (minutes > MAX_DURATION_MINUTES) {
      errors.endTime = MESSAGES.tooLong;
    }
  }

  return errors;
}
