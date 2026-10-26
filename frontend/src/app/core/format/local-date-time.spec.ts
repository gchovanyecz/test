import { parseLocalDateTime, toIsoInstant } from './local-date-time';

const isBudapest = (): boolean =>
  Intl.DateTimeFormat().resolvedOptions().timeZone === 'Europe/Budapest';

/** True when the local zone skips 02:30 on 31 March 2030 (the spring DST change in Central Europe). */
const hasSpringGapOn2030March31 = (): boolean => new Date(2030, 2, 31, 2, 30).getHours() !== 2;

describe('local-date-time', () => {
  describe('toIsoInstant', () => {
    it('converts a local date and time to the matching UTC instant', () => {
      expect(toIsoInstant('2030-01-07', '09:15')).toBe(new Date(2030, 0, 7, 9, 15).toISOString());
    });

    it('keeps the wall-clock time when the instant is read back in local time', () => {
      const iso = toIsoInstant('2030-11-04', '17:45');
      const back = new Date(iso as string);
      expect(back.getHours()).toBe(17);
      expect(back.getMinutes()).toBe(45);
    });

    it('uses the offset that applies on the given date (DST-sensitive in Budapest)', () => {
      // Budapest is UTC+1 in winter and UTC+2 in summer, so the same wall-clock time maps
      // to different UTC hours depending on the date.
      expect(toIsoInstant('2030-01-07', '12:00')).toBe(
        isBudapest() ? '2030-01-07T11:00:00.000Z' : new Date(2030, 0, 7, 12).toISOString(),
      );
      expect(toIsoInstant('2030-07-01', '12:00')).toBe(
        isBudapest() ? '2030-07-01T10:00:00.000Z' : new Date(2030, 6, 1, 12).toISOString(),
      );
    });

    // 02:30 on the spring-forward day is skipped in the local zone; JavaScript would move it to 03:30.
    // Runs only where the local zone has that gap (e.g. Europe/Budapest).
    it.runIf(hasSpringGapOn2030March31())(
      'returns null for a local time that does not exist because of the DST change',
      () => {
        expect(toIsoInstant('2030-03-31', '02:30')).toBeNull();
      },
    );

    it('returns null for malformed or impossible values', () => {
      expect(toIsoInstant('', '09:00')).toBeNull();
      expect(toIsoInstant('2030-01-07', '')).toBeNull();
      expect(toIsoInstant('2030-1-7', '09:00')).toBeNull();
      expect(toIsoInstant('2030-02-30', '09:00')).toBeNull();
      expect(toIsoInstant('2030-01-07', '24:00')).toBeNull();
      expect(toIsoInstant('2030-01-07', '09:60')).toBeNull();
    });
  });

  describe('parseLocalDateTime', () => {
    it('returns the local Date for a valid pair', () => {
      const parsed = parseLocalDateTime('2030-01-07', '23:45');
      expect(parsed?.getFullYear()).toBe(2030);
      expect(parsed?.getMonth()).toBe(0);
      expect(parsed?.getDate()).toBe(7);
      expect(parsed?.getHours()).toBe(23);
      expect(parsed?.getMinutes()).toBe(45);
    });
  });
});
