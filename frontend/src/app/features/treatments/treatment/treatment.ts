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
import { AddProblem } from '../components/add-problem/add-problem';
import { MedicineSelector } from '../components/medicine-selector/medicine-selector';
import { PatientHealthSnapshotPanel } from '../components/health-snapshot/health-snapshot';
import { ScanUploader } from '../components/scan-uploader/scan-uploader';
import { TreatmentPatientSelector } from '../components/patient-selector/patient-selector';
import { TreatmentProblemList } from '../components/problem-list/problem-list';
import {
  ClinicalProblem,
  PatientHealthSnapshot,
  PrescriptionItem,
  TreatmentCreateRequest
} from '../models/treatment.model';
import { TreatmentService } from '../services/treatment.service';

export const TREATMENT_AMOUNT_PATTERN = /^\d{1,8}(\.\d{1,2})?$/;

@Component({
  selector: 'app-treatment',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TreatmentPatientSelector,
    AddProblem,
    TreatmentProblemList,
    MedicineSelector,
    ScanUploader,
    PatientHealthSnapshotPanel
  ],
  templateUrl: './treatment.html',
  styleUrl: './treatment.scss'
})
export class Treatment implements OnDestroy {

  @ViewChild(TreatmentPatientSelector)
  private patientSelector?: TreatmentPatientSelector;

  private fb = inject(FormBuilder);

  form = this.fb.group({
    patientId: ['', Validators.required],
    treatmentDate: [this.today(), Validators.required],
    treatmentType: ['ADVISED', Validators.required],
    treatmentAmount: ['', [
      Validators.required,
      Validators.min(0),
      Validators.pattern(TREATMENT_AMOUNT_PATTERN)
    ]],
    doctorNotes: ['']
  });

  problems: ClinicalProblem[] = [];
  medicines: PrescriptionItem[] = [];
  scans: File[] = [];
  healthSnapshot: PatientHealthSnapshot | null = null;
  editingProblem: ClinicalProblem | null = null;
  editingProblemIndex: number | null = null;

  addProblemOpen = false;
  medicineOpen = false;
  loadingHealth = false;
  saving = false;
  snackbarMessage = '';
  snackbarType: 'success' | 'error' = 'success';

  private snackbarTimeout: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private treatmentService: TreatmentService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnDestroy(): void {
    if (this.snackbarTimeout) {
      clearTimeout(this.snackbarTimeout);
    }
  }

  onPatientChange(patientId: string | null): void {
    this.form.patchValue({
      patientId: patientId ?? ''
    });
    this.healthSnapshot = null;

    if (!patientId) {
      return;
    }

    this.loadingHealth = true;

    this.treatmentService
      .getLatestHealthSnapshot(patientId)
      .pipe(finalize(() => {
        this.loadingHealth = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: snapshot => {
          this.healthSnapshot = snapshot;
        },
        error: () => {
          this.healthSnapshot = null;
        }
      });
  }

  addProblem(problem: ClinicalProblem): void {
    if (this.editingProblemIndex !== null) {
      this.problems = this.problems.map((item, index) =>
        index === this.editingProblemIndex ? problem : item
      );
    } else {
      this.problems = [
        ...this.problems,
        problem
      ];
    }

    this.editingProblem = null;
    this.editingProblemIndex = null;
  }

  removeProblem(index: number): void {
    this.problems = this.problems.filter((_, itemIndex) => itemIndex !== index);
  }

  editProblem(index: number): void {
    this.editingProblem = this.problems[index];
    this.editingProblemIndex = index;
    this.addProblemOpen = true;
  }

  reset(): void {
    this.form.reset({
      patientId: '',
      treatmentDate: this.today(),
      treatmentType: 'ADVISED',
      treatmentAmount: '',
      doctorNotes: ''
    });
    this.problems = [];
    this.medicines = [];
    this.scans = [];
    this.healthSnapshot = null;
    this.patientSelector?.clear();
  }

  save(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const request: TreatmentCreateRequest = {
      patientId: value.patientId ?? '',
      treatmentDate: this.toIsoDate(value.treatmentDate ?? this.today()),
      treatmentType: value.treatmentType as 'ADVISED' | 'DESIRED',
      treatmentAmount: Number(value.treatmentAmount),
      doctorNotes: value.doctorNotes?.trim() || null,
      problems: this.problems,
      prescribedMedicines: this.medicines.map(medicine => ({
        medicineId: medicine.medicineId,
        quantity: medicine.quantity
      }))
    };

    this.saving = true;

    this.treatmentService
      .createTreatment(request, this.scans)
      .pipe(finalize(() => {
        this.saving = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: () => {
          this.showSnackbar('Treatment Saved Successfully', 'success');
          this.reset();
        },
        error: error => {
          this.showSnackbar(
            error?.error?.message ?? 'Could not save treatment.',
            'error'
          );
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

    this.cdr.detectChanges();
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }

  private toIsoDate(value: string): string {
    return new Date(`${value}T00:00:00`).toISOString();
  }
}
