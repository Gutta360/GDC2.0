import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  OnDestroy
} from '@angular/core';
import { finalize } from 'rxjs';
import { TreatmentPatientSelector } from '../../treatments/components/patient-selector/patient-selector';
import { PaymentResponse } from '../models/payment.model';
import { PaymentService } from '../services/payment.service';

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

  payments: PaymentResponse[] = [];
  loading = false;
  selectedPatientId = '';
  snackbarMessage = '';
  snackbarType: 'success' | 'error' = 'success';

  private snackbarTimeout: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private paymentService: PaymentService,
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

    this.paymentService
      .getPayments(patientId)
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: payments => {
          this.payments = payments;
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
}
