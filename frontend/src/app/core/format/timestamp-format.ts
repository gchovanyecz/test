/**
 * Formats an instant (e.g. the `createdAt` of a booking) in the browser's local time zone,
 * e.g. "2026. 10. 26. 12:00", with the same `hu-HU` formatting as the other date helpers.
 */
const TIMESTAMP_FORMATTER = new Intl.DateTimeFormat('hu-HU', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
});

export function formatTimestamp(instant: string): string {
  return TIMESTAMP_FORMATTER.format(new Date(instant));
}
