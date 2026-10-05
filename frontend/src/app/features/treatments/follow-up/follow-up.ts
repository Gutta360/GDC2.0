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
  FollowUpContext,
  FollowUpCreateRequest,
  PatientHealthSnapshot,
  PrescriptionItem,
  ScanResponse,
  problemTypeLabels
} from '../models/treatment.model';
import { TreatmentService } from '../services/treatment.service';

@Component({
  selector: 'app-follow-up',
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
  templateUrl: './follow-up.html',
  styleUrl: './follow-up.scss'
})
export class FollowUp implements OnDestroy {

  @ViewChild(TreatmentPatientSelector)
  private patientSelector?: TreatmentPatientSelector;

  private fb = inject(FormBuilder);

  form = this.fb.group({
    patientId: ['', Validators.required],
    followUpDate: [this.today(), Validators.required],
    doctorNotes: ['']
  });

  newProblems: ClinicalProblem[] = [];
  medicines: PrescriptionItem[] = [];
  scans: File[] = [];
  healthSnapshot: PatientHealthSnapshot | null = null;
  context: FollowUpContext | null = null;
  editingProblem: ClinicalProblem | null = null;
  editingProblemIndex: number | null = null;

  addProblemOpen = false;
  medicineOpen = false;
  loadingHealth = false;
  loadingContext = false;
  saving = false;
  snackbarMessage = '';
  snackbarType: 'success' | 'error' = 'success';

  readonly problemTypeLabels = problemTypeLabels;

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
    this.newProblems = [];
    this.medicines = [];
    this.scans = [];
    this.healthSnapshot = null;
    this.context = null;

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

    this.loadingContext = true;
    this.treatmentService
      .getFollowUpContext(patientId)
      .pipe(finalize(() => {
        this.loadingContext = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: context => {
          this.context = context;
        },
        error: () => {
          this.context = null;
        }
      });
  }

  addProblem(problem: ClinicalProblem): void {
    if (this.editingProblemIndex !== null) {
      this.newProblems = this.newProblems.map((item, index) =>
        index === this.editingProblemIndex ? problem : item
      );
    } else {
      this.newProblems = [
        ...this.newProblems,
        problem
      ];
    }

    this.editingProblem = null;
    this.editingProblemIndex = null;
    this.addProblemOpen = false;
    this.cdr.detectChanges();
  }

  scanImageUrl(scan: ScanResponse): string {
    return this.treatmentService.scanImageUrl(scan.id);
  }

  removeProblem(index: number): void {
    this.newProblems = this.newProblems.filter((_, itemIndex) => itemIndex !== index);
  }

  editProblem(index: number): void {
    this.editingProblem = this.newProblems[index];
    this.editingProblemIndex = index;
    this.addProblemOpen = true;
  }

  reset(): void {
    this.form.reset({
      patientId: '',
      followUpDate: this.today(),
      doctorNotes: ''
    });
    this.newProblems = [];
    this.medicines = [];
    this.scans = [];
    this.healthSnapshot = null;
    this.context = null;
    this.patientSelector?.clear();
  }

  save(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();

    const request: FollowUpCreateRequest = {
      patientId: value.patientId ?? '',
      relatedTreatmentId: this.context?.latestTreatment?.treatmentId ?? null,
      followUpDate: this.toIsoDate(value.followUpDate ?? this.today()),
      doctorNotes: value.doctorNotes?.trim() || null,
      newProblems: this.newProblems,
      prescribedMedicines: this.medicines.map(medicine => ({
        medicineId: medicine.medicineId,
        quantity: medicine.quantity
      }))
    };

    this.saving = true;

    this.treatmentService
      .createFollowUp(request, this.scans)
      .pipe(finalize(() => {
        this.saving = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: () => {
          this.showSnackbar('Follow Up Saved Successfully', 'success');
          this.reset();
        },
        error: error => {
          this.showSnackbar(
            error?.error?.message ?? 'Could not save follow up.',
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
