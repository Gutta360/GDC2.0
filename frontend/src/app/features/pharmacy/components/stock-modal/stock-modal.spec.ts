import { isPastExpiryDate } from './stock-modal';

describe('Medicine stock modal validation', () => {
  it('rejects past expiry dates for new stock', () => {
    expect(isPastExpiryDate('2026-10-01', '2026-10-02')).toBe(true);
    expect(isPastExpiryDate('2026-10-02', '2026-10-02')).toBe(false);
    expect(isPastExpiryDate('2026-10-03', '2026-10-02')).toBe(false);
  });
});
