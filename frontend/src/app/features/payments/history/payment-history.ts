import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  OnDestroy
} from '@angular/core';
import {
  finalize,
  forkJoin
} from 'rxjs';
import { PharmacyPaymentResponse } from '../../pharmacy/models/pharmacy.model';
import { PharmacyService } from '../../pharmacy/services/pharmacy.service';
import { TreatmentPatientSelector } from '../../treatments/components/patient-selector/patient-selector';
import { PaymentResponse } from '../models/payment.model';
import { PaymentService } from '../services/payment.service';

interface PaymentHistoryItem {
  paymentId: string;
  paidAt: string;
  amount: number;
  details: string | null;
  paymentFor: string;
  referenceId: string;
  paymentMode: string;
}

export function paymentHistoryEmptyMessage(
  loading: boolean,
  selectedPatientId: string
): string {
  if (loading) {
    return 'Loading payment history...';
  }

  return selectedPatientId
    ? 'No payments found.'
    : 'Select a patient to view history.';
}

@Component({
  selector: 'app-payment-history',
  standalone: true,
  imports: [
    CommonModule,
    TreatmentPatientSelector
  ],
  templateUrl: './payment-history.html',
  styleUrl: './payment-history.scss'
})
export class PaymentHistory implements OnDestroy {

  payments: PaymentHistoryItem[] = [];
  loading = false;
  selectedPatientId = '';
  snackbarMessage = '';
  snackbarType: 'success' | 'error' = 'success';

  private snackbarTimeout: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private paymentService: PaymentService,
    private pharmacyService: PharmacyService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnDestroy(): void {
    if (this.snackbarTimeout) {
      clearTimeout(this.snackbarTimeout);
    }
  }

  get emptyMessage(): string {
    return paymentHistoryEmptyMessage(this.loading, this.selectedPatientId);
  }

  onPatientChange(patientId: string | null): void {
    this.selectedPatientId = patientId ?? '';
    this.payments = [];

    if (!patientId) {
      return;
    }

    this.loading = true;

    forkJoin({
      treatmentPayments: this.paymentService.getPayments(patientId),
      pharmacyPayments: this.pharmacyService.getPayments(patientId)
    })
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: ({ treatmentPayments, pharmacyPayments }) => {
          this.payments = [
            ...treatmentPayments.map(payment => this.toTreatmentPaymentItem(payment)),
            ...pharmacyPayments.map(payment => this.toPharmacyPaymentItem(payment))
          ].sort((first, second) =>
            new Date(second.paidAt).getTime() - new Date(first.paidAt).getTime()
          );
        },
        error: error => {
          this.showSnackbar(error?.error?.message ?? 'Could not load payment history.', 'error');
        }
      });
  }

  private showSnackbar(
    message: string,
    type: 'success' | 'error'
  ): void {
    this.snackbarMessage = message;
    this.snackbarType = type;

    if (this.snackbarTimeout) {
      clearTimeout(this.snackbarTimeout);
    }

    this.snackbarTimeout = setTimeout(() => {
      this.snackbarMessage = '';
      this.snackbarTimeout = null;
      this.cdr.detectChanges();
    }, 5000);
  }

  private toTreatmentPaymentItem(payment: PaymentResponse): PaymentHistoryItem {
    return {
      paymentId: payment.paymentId,
      paidAt: payment.paidAt,
      amount: payment.amount,
      details: payment.details,
      paymentFor: payment.paymentFor,
      referenceId: payment.treatmentId,
      paymentMode: payment.paymentMode
    };
  }

  private toPharmacyPaymentItem(payment: PharmacyPaymentResponse): PaymentHistoryItem {
    return {
      paymentId: payment.paymentId,
      paidAt: payment.paidAt,
      amount: payment.totalAmount,
      details: payment.details,
      paymentFor: 'Pharmacy',
      referenceId: '',
      paymentMode: payment.paymentMode
    };
  }
}
