import { TREATMENT_AMOUNT_PATTERN } from './treatment';

describe('Treatment amount validation', () => {
  it('accepts the backend precision limit', () => {
    expect(TREATMENT_AMOUNT_PATTERN.test('0')).toBe(true);
    expect(TREATMENT_AMOUNT_PATTERN.test('100')).toBe(true);
    expect(TREATMENT_AMOUNT_PATTERN.test('750')).toBe(true);
    expect(TREATMENT_AMOUNT_PATTERN.test('750.50')).toBe(true);
    expect(TREATMENT_AMOUNT_PATTERN.test('99999999.99')).toBe(true);
  });

  it('rejects values outside backend precision', () => {
    expect(TREATMENT_AMOUNT_PATTERN.test('100000000.00')).toBe(false);
    expect(TREATMENT_AMOUNT_PATTERN.test('100.123')).toBe(false);
  });
});
