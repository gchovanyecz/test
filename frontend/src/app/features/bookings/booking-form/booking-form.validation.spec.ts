import { BookingFormValue, MESSAGES, validateBookingForm } from './booking-form.validation';

/** A fixed "now" well before the dates used below, so the past-time rule is not time-dependent. */
const now = new Date(2030, 0, 1, 8, 0);

const hasSpringGapOn2030March31 = (): boolean => new Date(2030, 2, 31, 2, 30).getHours() !== 2;

function form(overrides: Partial<BookingFormValue> = {}): BookingFormValue {
  return {
    roomId: 1,
    title: 'Heti megbeszélés',
    date: '2030-01-07',
    startTime: '09:00',
    endTime: '10:00',
    ...overrides,
  };
}

describe('validateBookingForm', () => {
  it('accepts a complete, valid booking', () => {
    expect(validateBookingForm(form(), now)).toEqual({});
  });

  it('accepts an empty optional title', () => {
    expect(validateBookingForm(form({ title: '' }), now)).toEqual({});
  });

  describe('required fields', () => {
    it('reports every missing required field with a Hungarian message', () => {
      expect(
        validateBookingForm({ roomId: null, title: '', date: '', startTime: '', endTime: '' }, now),
      ).toEqual({
        roomId: MESSAGES.roomRequired,
        date: MESSAGES.dateRequired,
        startTime: MESSAGES.startRequired,
        endTime: MESSAGES.endRequired,
      });
    });
  });

  describe('title', () => {
    it('accepts a title of exactly 100 characters', () => {
      expect(validateBookingForm(form({ title: 'a'.repeat(100) }), now)).toEqual({});
    });

    it('rejects a title longer than 100 characters', () => {
      expect(validateBookingForm(form({ title: 'a'.repeat(101) }), now)).toEqual({
        title: 'A cím legfeljebb 100 karakter lehet.',
      });
    });
  });

  describe('start time', () => {
    it('rejects a start in the past', () => {
      const laterThanStart = new Date(2030, 0, 7, 8, 0);
      const errors = validateBookingForm(
        form({ startTime: '07:45', endTime: '09:00' }),
        laterThanStart,
      );
      expect(errors.startTime).toBe('A kezdés nem lehet a múltban.');
    });

    it('accepts a start that is still ahead of now', () => {
      const beforeStart = new Date(2030, 0, 7, 7, 0);
      expect(
        validateBookingForm(form({ startTime: '07:45', endTime: '09:00' }), beforeStart),
      ).toEqual({});
    });

    it('rejects a start that is not on the 15-minute grid', () => {
      expect(validateBookingForm(form({ startTime: '09:07' }), now)).toEqual({
        startTime: 'Az időpontnak 15 perces időrácsra kell esnie (pl. 9:00, 9:15).',
      });
    });

    it('accepts every quarter hour', () => {
      for (const time of ['09:00', '09:15', '09:30', '09:45']) {
        expect(validateBookingForm(form({ startTime: time, endTime: '11:00' }), now)).toEqual({});
      }
    });
  });

  describe('end time', () => {
    it('rejects an end before the start', () => {
      expect(validateBookingForm(form({ startTime: '10:00', endTime: '09:00' }), now)).toEqual({
        endTime: 'A befejezésnek a kezdés után kell lennie.',
      });
    });

    it('rejects an end equal to the start', () => {
      expect(validateBookingForm(form({ startTime: '10:00', endTime: '10:00' }), now).endTime).toBe(
        'A befejezésnek a kezdés után kell lennie.',
      );
    });

    it('rejects an end that is not on the 15-minute grid', () => {
      expect(validateBookingForm(form({ endTime: '10:07' }), now).endTime).toBe(
        'Az időpontnak 15 perces időrácsra kell esnie (pl. 9:00, 9:15).',
      );
    });
  });

  describe('duration', () => {
    it('accepts the minimum duration of 15 minutes', () => {
      expect(validateBookingForm(form({ startTime: '09:00', endTime: '09:15' }), now)).toEqual({});
    });

    it('accepts the maximum duration of 8 hours', () => {
      expect(validateBookingForm(form({ startTime: '09:00', endTime: '17:00' }), now)).toEqual({});
    });

    it('rejects a duration above 8 hours', () => {
      expect(validateBookingForm(form({ startTime: '09:00', endTime: '17:15' }), now).endTime).toBe(
        'A foglalás legfeljebb 8 óra lehet.',
      );
    });

    it('does not report a duration error when the order is already wrong', () => {
      expect(validateBookingForm(form({ startTime: '10:00', endTime: '09:00' }), now).endTime).toBe(
        'A befejezésnek a kezdés után kell lennie.',
      );
    });
  });

  // Runs only where the local zone skips 02:30 on that day (e.g. Europe/Budapest).
  it.runIf(hasSpringGapOn2030March31())(
    'reports a local time that does not exist on the DST change day',
    () => {
      expect(
        validateBookingForm(
          form({ date: '2030-03-31', startTime: '02:30', endTime: '03:30' }),
          new Date(2030, 2, 1),
        ).startTime,
      ).toBe(MESSAGES.nonexistentTime);
    },
  );
});
