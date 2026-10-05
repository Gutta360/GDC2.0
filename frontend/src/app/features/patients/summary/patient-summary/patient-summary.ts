import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  OnDestroy
} from '@angular/core';
import {
  catchError,
  finalize,
  forkJoin,
  of
} from 'rxjs';
import { PaymentResponse } from '../../../payments/models/payment.model';
import { PaymentService } from '../../../payments/services/payment.service';
import { PharmacyPaymentResponse } from '../../../pharmacy/models/pharmacy.model';
import { PharmacyService } from '../../../pharmacy/services/pharmacy.service';
import { TreatmentPatientSelector } from '../../../treatments/components/patient-selector/patient-selector';
import {
  ClinicalProblem,
  FollowUpResponse,
  PrescriptionItem,
  ScanResponse,
  TreatmentResponse,
  problemTypeLabels
} from '../../../treatments/models/treatment.model';
import { TreatmentService } from '../../../treatments/services/treatment.service';

interface SummaryPayment {
  id: string;
  details: string;
  purpose: string;
  amount: number;
  mode: string;
  paidAt: string;
}

@Component({
  selector: 'app-patient-summary',
  standalone: true,
  imports: [
    CommonModule,
    TreatmentPatientSelector
  ],
  templateUrl: './patient-summary.html',
  styleUrl: './patient-summary.scss'
})
export class PatientSummary implements OnDestroy {

  selectedPatientId = '';
  treatments: TreatmentResponse[] = [];
  followUps: FollowUpResponse[] = [];
  treatmentPayments: PaymentResponse[] = [];
  pharmacyPayments: PharmacyPaymentResponse[] = [];
  loading = false;
  errorMessage = '';

  private snackbarTimeout: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private treatmentService: TreatmentService,
    private paymentService: PaymentService,
    private pharmacyService: PharmacyService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnDestroy(): void {
    if (this.snackbarTimeout) {
      clearTimeout(this.snackbarTimeout);
    }
  }

  get chiefComplaint(): TreatmentResponse | null {
    return [...this.treatments]
      .sort((first, second) =>
        new Date(first.treatmentDate).getTime() - new Date(second.treatmentDate).getTime()
      )[0] ?? null;
  }

  get totalAmount(): number {
    return this.treatments.reduce((total, treatment) =>
      total + Number(treatment.treatmentAmount || 0), 0
    );
  }

  get totalPaid(): number {
    return this.treatmentPayments.reduce((total, payment) =>
      total + Number(payment.amount || 0), 0
    );
  }

  get paidPercent(): number {
    if (this.totalAmount <= 0) {
      return 0;
    }

    return Math.min(100, Math.round((this.totalPaid / this.totalAmount) * 100));
  }

  get progressWidth(): string {
    return `${this.paidPercent}%`;
  }

  get payments(): SummaryPayment[] {
    const treatmentItems = this.treatmentPayments.map(payment => ({
      id: payment.paymentId,
      details: payment.details || '-',
      purpose: payment.treatmentId
        ? `${payment.paymentFor} ${payment.treatmentId}`
        : payment.paymentFor,
      amount: payment.amount,
      mode: payment.paymentMode,
      paidAt: payment.paidAt
    }));

    const pharmacyItems = this.pharmacyPayments.map(payment => ({
      id: payment.paymentId,
      details: payment.details || '-',
      purpose: 'Pharmacy',
      amount: payment.totalAmount,
      mode: payment.paymentMode,
      paidAt: payment.paidAt
    }));

    return [...treatmentItems, ...pharmacyItems]
      .sort((first, second) =>
        new Date(second.paidAt).getTime() - new Date(first.paidAt).getTime()
      );
  }

  onPatientChange(patientId: string | null): void {
    this.selectedPatientId = patientId ?? '';
    this.treatments = [];
    this.followUps = [];
    this.treatmentPayments = [];
    this.pharmacyPayments = [];
    this.errorMessage = '';

    if (!patientId) {
      return;
    }

    this.loading = true;

    forkJoin({
      treatments: this.treatmentService.getTreatments(patientId).pipe(catchError(() => of([]))),
      followUps: this.treatmentService.getFollowUps(patientId).pipe(catchError(() => of([]))),
      treatmentPayments: this.paymentService.getPayments(patientId).pipe(catchError(() => of([]))),
      pharmacyPayments: this.pharmacyService.getPayments(patientId).pipe(catchError(() => of([])))
    })
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: result => {
          this.treatments = result.treatments;
          this.followUps = result.followUps;
          this.treatmentPayments = result.treatmentPayments;
          this.pharmacyPayments = result.pharmacyPayments;
        },
        error: () => {
          this.errorMessage = 'Could not load patient summary.';
          this.clearErrorLater();
        }
      });
  }

  scanImageUrl(scan: ScanResponse): string {
    return this.treatmentService.scanImageUrl(scan.id);
  }

  treatmentTypeLabel(value: string | null | undefined): string {
    if (!value) {
      return '--';
    }

    return value.charAt(0) + value.slice(1).toLowerCase();
  }

  problemsText(problems: ClinicalProblem[]): string {
    if (!problems.length) {
      return 'No problems found';
    }

    return problems
      .map(problem => {
        const label = problemTypeLabels[problem.problemType] ?? problem.problemType;
        const teeth = problem.teeth.length
          ? `Teeth: ${problem.teeth.join(', ')}`
          : 'Teeth: --';
        const notes = problem.notes
          ? ` - ${problem.notes}`
          : '';

        return `${teeth} - ${label}${notes}`;
      })
      .join('; ');
  }

  medicinesText(medicines: PrescriptionItem[]): string {
    if (!medicines.length) {
      return 'No medicines prescribed';
    }

    return medicines
      .map(medicine => `${medicine.medicineName} : ${medicine.quantity}`)
      .join(', ');
  }

  formatDate(value: string): string {
    return new Intl.DateTimeFormat('en-IN', {
      weekday: 'long',
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: 'numeric',
      minute: '2-digit'
    }).format(new Date(value));
  }

  formatPaidAt(value: string): string {
    return new Intl.DateTimeFormat('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: 'numeric',
      minute: '2-digit'
    }).format(new Date(value));
  }

  private clearErrorLater(): void {
    if (this.snackbarTimeout) {
      clearTimeout(this.snackbarTimeout);
    }

    this.snackbarTimeout = setTimeout(() => {
      this.errorMessage = '';
      this.snackbarTimeout = null;
      this.cdr.detectChanges();
    }, 5000);
  }
}
