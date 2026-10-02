import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { PatientHealthSnapshot } from '../../models/treatment.model';

@Component({
  selector: 'app-patient-health-snapshot',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './health-snapshot.html',
  styleUrl: './health-snapshot.scss'
})
export class PatientHealthSnapshotPanel {

  @Input() snapshot: PatientHealthSnapshot | null = null;
  @Input() loading = false;

  hasAnyCondition(): boolean {
    if (!this.snapshot) {
      return false;
    }

    return [
      this.snapshot.hasDiabetes,
      this.snapshot.hasHypertension,
      this.snapshot.hasHeartDisease,
      this.snapshot.hasAsthma,
      this.snapshot.hasKidneyDisease,
      this.snapshot.hasLiverDisease,
      this.snapshot.hasThyroidDisorder,
      this.snapshot.hasBleedingDisorders,
      this.snapshot.hasNeurologicalIssues
    ].some(Boolean);
  }

  yesNo(value: boolean): string {
    return value ? 'Yes' : 'No';
  }
}
