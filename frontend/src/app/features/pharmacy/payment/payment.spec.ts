import { PHARMACY_MONEY_PATTERN } from './payment';

describe('Pharmacy payment validation', () => {
  it('matches backend money precision', () => {
    expect(PHARMACY_MONEY_PATTERN.test('0')).toBe(true);
    expect(PHARMACY_MONEY_PATTERN.test('50')).toBe(true);
    expect(PHARMACY_MONEY_PATTERN.test('50.25')).toBe(true);
    expect(PHARMACY_MONEY_PATTERN.test('99999999.99')).toBe(true);
    expect(PHARMACY_MONEY_PATTERN.test('100000000.00')).toBe(false);
    expect(PHARMACY_MONEY_PATTERN.test('50.255')).toBe(false);
  });
});
