import { paymentHistoryEmptyMessage } from './payment-history';

describe('Payment history empty state', () => {
  it('describes loading, empty, and initial states', () => {
    expect(paymentHistoryEmptyMessage(true, 'P-00001')).toBe('Loading payment history...');
    expect(paymentHistoryEmptyMessage(false, 'P-00001')).toBe('No payments found.');
    expect(paymentHistoryEmptyMessage(false, '')).toBe('Select a patient to view history.');
  });
});
