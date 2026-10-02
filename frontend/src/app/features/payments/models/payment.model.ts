export type PaymentMode =
  | 'CASH'
  | 'UPI';

export interface OutstandingTreatmentPayment {
  treatmentId: string;
  treatmentDate: string;
  treatmentType: 'ADVISED' | 'DESIRED';
  treatmentAmount: number;
  outstandingAmount: number;
}

export interface PaymentCreateRequest {
  patientId: string;
  treatmentId: string;
  paymentMode: PaymentMode;
  details: string | null;
}

export interface PaymentResponse {
  paymentId: string;
  patientId: string;
  patientName: string;
  treatmentId: string;
  treatmentDate: string;
  treatmentType: 'ADVISED' | 'DESIRED';
  paymentFor: string;
  paymentMode: PaymentMode;
  amount: number;
  details: string | null;
  paidAt: string;
  createdAt: string;
  updatedAt: string;
  version: number;
}
