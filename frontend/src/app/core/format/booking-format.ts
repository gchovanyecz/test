/**
 * Display helpers for bookings. Dates are rendered in the browser's local time zone
 * with Hungarian formatting (`hu-HU`), so the UI does not depend on Angular locale data.
 */

export const UNTITLED_BOOKING_LABEL = '(nincs cím)';

const DATE_FORMATTER = new Intl.DateTimeFormat('hu-HU', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long',
});

const TIME_FORMATTER = new Intl.DateTimeFormat('hu-HU', {
  hour: '2-digit',
  minute: '2-digit',
});

const DATE_TIME_FORMATTER = new Intl.DateTimeFormat('hu-HU', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
});

export function displayTitle(title: string | null): string {
  const trimmed = title?.trim();
  return trimmed ? trimmed : UNTITLED_BOOKING_LABEL;
}

/** The day of the booking start, e.g. "2026. november 2., hétfő". */
export function formatBookingDate(startTime: string): string {
  return DATE_FORMATTER.format(new Date(startTime));
}

/**
 * Start and end time. Same-day bookings show only the times ("09:00 – 10:30");
 * bookings that cross midnight also show the date of both ends.
 */
export function formatTimeRange(startTime: string, endTime: string): string {
  const start = new Date(startTime);
  const end = new Date(endTime);
  if (start.toDateString() === end.toDateString()) {
    return `${TIME_FORMATTER.format(start)} – ${TIME_FORMATTER.format(end)}`;
  }
  return `${DATE_TIME_FORMATTER.format(start)} – ${DATE_TIME_FORMATTER.format(end)}`;
}
