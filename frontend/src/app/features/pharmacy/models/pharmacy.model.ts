export type PaymentMode =
  | 'CASH'
  | 'UPI';

export interface Medicine {
  medicineId: string;
  medicineName: string;
  availableQuantity: number;
  expiryDate: string;
}

export interface MedicineStockRequest {
  medicineName: string;
  quantity: number;
  expiryDate: string;
}

export interface PendingPrescriptionItem {
  prescriptionItemId: number;
  sourceType: 'TREATMENT' | 'FOLLOW_UP';
  sourceId: string;
  sourceDate: string;
  medicineId: string;
  medicineName: string;
  prescribedQuantity: number;
  availableQuantity: number;
  expiryDate: string;
  expired: boolean;
}

export interface PharmacyPaymentItemRequest {
  prescriptionItemId: number;
  unitPrice: number;
}

export interface PharmacyPaymentCreateRequest {
  patientId: string;
  paymentMode: PaymentMode;
  details: string | null;
  items: PharmacyPaymentItemRequest[];
}

export interface PharmacyPaymentItemResponse {
  prescriptionItemId: number;
  medicineId: string;
  medicineName: string;
  prescribedQuantity: number;
  dispensedQuantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface PharmacyPaymentResponse {
  paymentId: string;
  patientId: string;
  patientName: string;
  paymentMode: PaymentMode;
  totalAmount: number;
  details: string | null;
  paidAt: string;
  items: PharmacyPaymentItemResponse[];
  createdAt: string;
  updatedAt: string;
  version: number;
}
