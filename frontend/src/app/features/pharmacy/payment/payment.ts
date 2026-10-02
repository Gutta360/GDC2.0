import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  ViewChild
} from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { finalize } from 'rxjs';
import { TreatmentPatientSelector } from '../../treatments/components/patient-selector/patient-selector';
import {
  PaymentMode,
  PendingPrescriptionItem,
  PharmacyPaymentCreateRequest
} from '../models/pharmacy.model';
import { PharmacyService } from '../services/pharmacy.service';

export const PHARMACY_MONEY_PATTERN = /^\d{1,8}(\.\d{1,2})?$/;

@Component({
  selector: 'app-pharmacy-payment',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TreatmentPatientSelector
  ],
  templateUrl: './payment.html',
  styleUrl: './payment.scss'
})
export class PharmacyPayment implements OnDestroy {

  private fb = inject(FormBuilder);

  @ViewChild(TreatmentPatientSelector)
  private patientSelector?: TreatmentPatientSelector;

  form = this.fb.group({
    patientId: ['', Validators.required],
    paymentMode: ['CASH' as PaymentMode, Validators.required],
    details: ['']
  });

  pendingItems: PendingPrescriptionItem[] = [];
  prices: Record<number, string> = {};
  loading = false;
  saving = false;
  snackbarMessage = '';
  snackbarType: 'success' | 'error' = 'success';

  private snackbarTimeout: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private pharmacyService: PharmacyService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnDestroy(): void {
    if (this.snackbarTimeout) {
      clearTimeout(this.snackbarTimeout);
    }
  }

  get totalAmount(): number {
    return this.pendingItems.reduce((total, item) => {
      const price = Number(this.prices[item.prescriptionItemId] || 0);
      return total + (Number.isFinite(price) ? price * item.prescribedQuantity : 0);
    }, 0);
  }

  get hasInvalidPrices(): boolean {
    return this.pendingItems.some(item => {
      const price = this.prices[item.prescriptionItemId] ?? '';
      return !PHARMACY_MONEY_PATTERN.test(price);
    });
  }

  get hasBlockedItems(): boolean {
    return this.pendingItems.some(item =>
      item.expired || item.availableQuantity < item.prescribedQuantity
    );
  }

  onPatientChange(patientId: string | null): void {
    this.form.patchValue({ patientId: patientId ?? '' });
    this.pendingItems = [];
    this.prices = {};

    if (!patientId) {
      return;
    }

    this.loading = true;

    this.pharmacyService
      .getPendingPrescriptions(patientId)
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: items => {
          this.pendingItems = items;
          this.prices = items.reduce<Record<number, string>>((prices, item) => {
            prices[item.prescriptionItemId] = '0.00';
            return prices;
          }, {});
        },
        error: error => {
          this.showSnackbar(error?.error?.message ?? 'Could not load pending medicines.', 'error');
        }
      });
  }

  onPriceChange(
    prescriptionItemId: number,
    event: Event
  ): void {
    const input = event.target as HTMLInputElement | null;
    this.prices[prescriptionItemId] = input?.value ?? '';
  }

  pay(): void {
    if (
      this.form.invalid ||
      this.saving ||
      !this.pendingItems.length ||
      this.hasInvalidPrices ||
      this.hasBlockedItems
    ) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    if (value.paymentMode === 'UPI' && !(value.details ?? '').trim()) {
      this.showSnackbar('Payment details are required for UPI.', 'error');
      return;
    }

    const request: PharmacyPaymentCreateRequest = {
      patientId: value.patientId ?? '',
      paymentMode: value.paymentMode as PaymentMode,
      details: value.details?.trim() || null,
      items: this.pendingItems.map(item => ({
        prescriptionItemId: item.prescriptionItemId,
        unitPrice: Number(this.prices[item.prescriptionItemId])
      }))
    };

    this.saving = true;

    this.pharmacyService
      .createPayment(request)
      .pipe(finalize(() => {
        this.saving = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: () => {
          this.showSnackbar('Pharmacy Payment Saved Successfully', 'success');
          this.reset();
        },
        error: error => {
          this.showSnackbar(error?.error?.message ?? 'Could not save pharmacy payment.', 'error');
        }
      });
  }

  reset(): void {
    this.form.reset({
      patientId: '',
      paymentMode: 'CASH',
      details: ''
    });
    this.pendingItems = [];
    this.prices = {};
    this.patientSelector?.clear();
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
