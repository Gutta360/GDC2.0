import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  OnInit,
  Output
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PatientSummary } from '../../../patients/models/patient.model';
import { PatientService } from '../../../patients/services/patient.service';

@Component({
  selector: 'app-treatment-patient-selector',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './patient-selector.html',
  styleUrl: './patient-selector.scss'
})
export class TreatmentPatientSelector implements OnInit {

  @Output() patientChange = new EventEmitter<string | null>();

  patients: PatientSummary[] = [];
  selectedPatientId = '';
  search = '';
  dropdownOpen = false;
  loading = false;

  constructor(
    private patientService: PatientService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loading = true;

    this.patientService
      .getAllPatients()
      .subscribe({
        next: patients => {
          this.patients = patients
            .filter(patient => patient.active)
            .map(patient => ({
              patientId: patient.patientId,
              firstName: patient.firstName,
              lastName: patient.lastName,
              fullName: patient.fullName,
              mobile: patient.mobile
            }));
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.patients = [];
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
  }

  get selectedLabel(): string {
    const selected = this.patients.find(
      patient => patient.patientId === this.selectedPatientId
    );

    if (!selected) {
      return 'Select patient';
    }

    return this.labelFor(selected);
  }

  get filteredPatients(): PatientSummary[] {
    const query = this.search.trim().toLowerCase();

    if (!query) {
      return this.patients;
    }

    return this.patients.filter(patient =>
      this.labelFor(patient).toLowerCase().includes(query) ||
      (patient.mobile ?? '').includes(query)
    );
  }

  toggleDropdown(): void {
    this.dropdownOpen = !this.dropdownOpen;
  }

  selectPatient(patient: PatientSummary): void {
    this.selectedPatientId = patient.patientId;
    this.search = '';
    this.dropdownOpen = false;
    this.patientChange.emit(patient.patientId);
  }

  clear(): void {
    this.selectedPatientId = '';
    this.search = '';
    this.dropdownOpen = false;
    this.patientChange.emit(null);
  }

  labelFor(patient: PatientSummary): string {
    return `${patient.patientId}  ${patient.fullName}`.trim();
  }
}
