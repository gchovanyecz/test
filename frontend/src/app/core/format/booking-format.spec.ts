import {
  displayTitle,
  formatBookingDate,
  formatTimeRange,
  UNTITLED_BOOKING_LABEL,
} from './booking-format';

/** Local wall-clock time as an ISO instant, so the expectations hold in any time zone. */
function localIso(year: number, month: number, day: number, hour: number, minute = 0): string {
  return new Date(year, month, day, hour, minute).toISOString();
}

describe('booking-format', () => {
  describe('displayTitle', () => {
    it('returns the Hungarian placeholder for a missing title', () => {
      expect(displayTitle(null)).toBe(UNTITLED_BOOKING_LABEL);
      expect(displayTitle('   ')).toBe(UNTITLED_BOOKING_LABEL);
    });

    it('returns the trimmed title when one is set', () => {
      expect(displayTitle('  Heti megbeszélés ')).toBe('Heti megbeszélés');
    });
  });

  describe('formatBookingDate', () => {
    it('formats the start day in Hungarian with the weekday', () => {
      expect(formatBookingDate(localIso(2026, 10, 2, 9))).toBe('2026. november 2., hétfő');
    });
  });

  describe('formatTimeRange', () => {
    it('shows only the times for a booking within one day', () => {
      expect(formatTimeRange(localIso(2026, 10, 2, 9), localIso(2026, 10, 2, 10, 30))).toBe(
        '09:00 – 10:30',
      );
    });

    it('shows the date with both ends for a booking that crosses midnight', () => {
      expect(formatTimeRange(localIso(2026, 10, 2, 23), localIso(2026, 10, 3, 1))).toBe(
        '2026. 11. 02. 23:00 – 2026. 11. 03. 01:00',
      );
    });
  });
});
