/**
 * Conversion between the wall-clock values of native `<input type="date">` / `<input type="time">`
 * fields (`yyyy-mm-dd`, `HH:mm`) and instants, interpreted in the browser's local time zone.
 */

const DATE_PATTERN = /^(\d{4})-(\d{2})-(\d{2})$/;
const TIME_PATTERN = /^(\d{2}):(\d{2})$/;

/**
 * The local Date for a date and time pair, or `null` when the pair is malformed or does not
 * exist in the local time zone. JavaScript silently moves a time skipped by a DST change
 * (e.g. 02:30 on the spring-forward day) forward, so the result is checked field by field.
 */
export function parseLocalDateTime(date: string, time: string): Date | null {
  const dateParts = DATE_PATTERN.exec(date);
  const timeParts = TIME_PATTERN.exec(time);
  if (!dateParts || !timeParts) {
    return null;
  }

  const year = Number(dateParts[1]);
  const month = Number(dateParts[2]);
  const day = Number(dateParts[3]);
  const hour = Number(timeParts[1]);
  const minute = Number(timeParts[2]);
  if (month < 1 || month > 12 || day < 1 || day > 31 || hour > 23 || minute > 59) {
    return null;
  }

  const local = new Date(year, month - 1, day, hour, minute);
  const exists =
    local.getFullYear() === year &&
    local.getMonth() === month - 1 &&
    local.getDate() === day &&
    local.getHours() === hour &&
    local.getMinutes() === minute;
  return exists ? local : null;
}

/** The instant (ISO-8601, UTC `Z`) of a local date and time, or `null` if it does not exist. */
export function toIsoInstant(date: string, time: string): string | null {
  return parseLocalDateTime(date, time)?.toISOString() ?? null;
}
