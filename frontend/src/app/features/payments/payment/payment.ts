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
  OutstandingTreatmentPayment,
  PaymentCreateRequest,
  PaymentMode
} from '../models/payment.model';
import { PaymentService } from '../services/payment.service';
import { DecimalInputDirective } from '../../../shared/directives/decimal-input.directive';

export function canSubmitPayment(
  formInvalid: boolean,
  saving: boolean,
  hasSelectedTreatment: boolean
): boolean {
  return !formInvalid && !saving && hasSelectedTreatment;
}

export const PAYMENT_AMOUNT_PATTERN = /^\d{1,8}(\.\d{1,2})?$/;

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TreatmentPatientSelector,
    DecimalInputDirective
  ],
  templateUrl: './payment.html',
  styleUrl: './payment.scss'
})
export class Payment implements OnDestroy {

  private fb = inject(FormBuilder);

  @ViewChild(TreatmentPatientSelector)
  private patientSelector?: TreatmentPatientSelector;

  form = this.fb.group({
    patientId: ['', Validators.required],
    treatmentId: ['', Validators.required],
    paymentAmount: ['0', [
      Validators.required,
      Validators.min(0),
      Validators.pattern(PAYMENT_AMOUNT_PATTERN)
    ]],
    paymentMode: ['CASH' as PaymentMode, Validators.required],
    details: ['', Validators.maxLength(1000)]
  });

  outstandingTreatments: OutstandingTreatmentPayment[] = [];
  loading = false;
  saving = false;
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

  get selectedTreatment(): OutstandingTreatmentPayment | null {
    const treatmentId = this.form.controls.treatmentId.value;
    return this.outstandingTreatments.find(treatment => treatment.treatmentId === treatmentId) ?? null;
  }

  get payableAmount(): number {
    return this.selectedTreatment?.outstandingAmount ?? 0;
  }

  get submitDisabled(): boolean {
    return !canSubmitPayment(this.form.invalid, this.saving, !!this.selectedTreatment);
  }

  onPatientChange(patientId: string | null): void {
    this.form.patchValue({
      patientId: patientId ?? '',
      treatmentId: '',
      paymentAmount: '0'
    });
    this.outstandingTreatments = [];

    if (!patientId) {
      return;
    }

    this.loading = true;

    this.paymentService
      .getOutstandingTreatments(patientId)
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: treatments => {
          this.outstandingTreatments = treatments;
          if (treatments.length === 1) {
            this.selectTreatment(treatments[0]);
          }
        },
        error: error => {
          this.showSnackbar(error?.error?.message ?? 'Could not load outstanding treatments.', 'error');
        }
      });
  }

  selectTreatment(treatment: OutstandingTreatmentPayment): void {
    this.form.patchValue({
      treatmentId: treatment.treatmentId,
      paymentAmount: treatment.outstandingAmount.toFixed(2)
    });
  }

  pay(): void {
    if (!canSubmitPayment(this.form.invalid, this.saving, !!this.selectedTreatment)) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const request: PaymentCreateRequest = {
      patientId: value.patientId ?? '',
      treatmentId: value.treatmentId ?? '',
      amount: Number(value.paymentAmount ?? 0),
      paymentMode: value.paymentMode as PaymentMode,
      details: value.details?.trim() || null
    };

    this.saving = true;

    this.paymentService
      .createPayment(request)
      .pipe(finalize(() => {
        this.saving = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: () => {
          this.showSnackbar('Payment Saved Successfully', 'success');
          const patientId = request.patientId;
          this.form.patchValue({
            treatmentId: '',
            paymentAmount: '0',
            details: ''
          });
          this.reloadOutstanding(patientId);
        },
        error: error => {
          this.showSnackbar(error?.error?.message ?? 'Could not save payment.', 'error');
        }
      });
  }

  reset(): void {
    this.form.reset({
      patientId: '',
      treatmentId: '',
      paymentAmount: '0',
      paymentMode: 'CASH',
      details: ''
    });
    this.outstandingTreatments = [];
    this.patientSelector?.clear();
  }

  treatmentLabel(treatment: OutstandingTreatmentPayment): string {
    return `${treatment.treatmentId} · ${this.formatDate(treatment.treatmentDate)} · ${treatment.treatmentType}`;
  }

  private reloadOutstanding(patientId: string): void {
    this.loading = true;

    this.paymentService
      .getOutstandingTreatments(patientId)
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: treatments => {
          this.outstandingTreatments = treatments;
          if (treatments.length === 1) {
            this.selectTreatment(treatments[0]);
          }
        },
        error: error => {
          this.showSnackbar(error?.error?.message ?? 'Could not refresh outstanding treatments.', 'error');
        }
      });
  }

  private formatDate(value: string): string {
    return new Intl.DateTimeFormat('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    }).format(new Date(value));
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
