import { canSubmitPayment } from './payment';

describe('Payment submit rules', () => {
  it('allows submit only for a valid form with a selected treatment while not saving', () => {
    expect(canSubmitPayment(false, false, true)).toBe(true);
    expect(canSubmitPayment(true, false, true)).toBe(false);
    expect(canSubmitPayment(false, true, true)).toBe(false);
    expect(canSubmitPayment(false, false, false)).toBe(false);
  });
});
