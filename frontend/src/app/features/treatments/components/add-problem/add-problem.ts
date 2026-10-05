import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalInputDirective } from '../../../../shared/directives/decimal-input.directive';
import {
  ClinicalProblem,
  ImplantType,
  ImpactionType,
  ProblemType,
  RootCanalLength,
  impactionTypeLabels,
  implantTypeLabels,
  problemTypeLabels
} from '../../models/treatment.model';

interface RootCanalDraft {
  lengthMm: string;
  notes: string;
}

@Component({
  selector: 'app-add-problem',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    DecimalInputDirective
  ],
  templateUrl: './add-problem.html',
  styleUrl: './add-problem.scss'
})
export class AddProblem implements OnChanges {

  @Input() open = false;
  @Input() initialProblem: ClinicalProblem | null = null;

  @Output() close = new EventEmitter<void>();
  @Output() problemAdd = new EventEmitter<ClinicalProblem>();

  readonly quadrants = [
    { title: 'Upper Left', teeth: [18, 17, 16, 15, 14, 13, 12, 11] },
    { title: 'Upper Right', teeth: [21, 22, 23, 24, 25, 26, 27, 28] },
    { title: 'Lower Left', teeth: [48, 47, 46, 45, 44, 43, 42, 41] },
    { title: 'Lower Right', teeth: [31, 32, 33, 34, 35, 36, 37, 38] }
  ];

  readonly problemOptions = Object.entries(problemTypeLabels)
    .map(([value, label]) => ({
      value: value as ProblemType,
      label
    }));

  readonly impactionOptions = Object.entries(impactionTypeLabels)
    .map(([value, label]) => ({
      value: value as ImpactionType,
      label
    }));

  readonly implantOptions = Object.entries(implantTypeLabels)
    .map(([value, label]) => ({
      value: value as ImplantType,
      label
    }));

  selectedTeeth = new Set<number>();
  problemType: ProblemType | '' = '';
  notes = '';
  errorMessage = '';

  impactionType: ImpactionType | '' = '';

  implantType: ImplantType | '' = '';
  implantWidthMm = '';
  implantLengthMm = '';
  implantCompany = '';
  healingCapPlacementDate = '';
  abutmentPlacementDate = '';
  crownPlacementDate = '';

  rootCanalDrafts: Record<number, Record<string, RootCanalDraft>> = {};

  ngOnChanges(): void {
    if (this.open && this.initialProblem) {
      this.populate(this.initialProblem);
    } else if (this.open && !this.initialProblem) {
      this.reset();
    }
  }

  toggleTooth(tooth: number): void {
    if (this.selectedTeeth.has(tooth)) {
      this.selectedTeeth.delete(tooth);
      delete this.rootCanalDrafts[tooth];
    } else {
      this.selectedTeeth.add(tooth);
      this.ensureRootCanalDraft(tooth);
    }
  }

  onProblemTypeChange(): void {
    this.errorMessage = '';

    if (this.problemType !== 'ROOT_CANAL') {
      this.rootCanalDrafts = {};
    } else {
      Array.from(this.selectedTeeth)
        .forEach(tooth => this.ensureRootCanalDraft(tooth));
    }

    if (this.problemType !== 'IMPACTION') {
      this.impactionType = '';
    }

    if (this.problemType !== 'IMPLANTS') {
      this.implantType = '';
      this.implantWidthMm = '';
      this.implantLengthMm = '';
      this.implantCompany = '';
      this.healingCapPlacementDate = '';
      this.abutmentPlacementDate = '';
      this.crownPlacementDate = '';
    }
  }

  canalsForTooth(tooth: number): string[] {
    if ([11, 21, 31, 41, 12, 22, 32, 42, 13, 23, 33, 43].includes(tooth)) {
      return ['Single'];
    }

    if ([14, 24, 15, 25].includes(tooth)) {
      return ['Buccal', 'Palatal'];
    }

    if ([34, 44, 35, 45].includes(tooth)) {
      return ['Buccal', 'Lingual'];
    }

    if ([16, 26, 17, 27, 18, 28].includes(tooth)) {
      return ['Palatal', 'Mesial', 'Distal'];
    }

    if ([36, 46, 37, 47, 38, 48].includes(tooth)) {
      return ['Mesial', 'Distal', 'Lingual', 'Distal 2'];
    }

    return [];
  }

  sortedSelectedTeeth(): number[] {
    return Array.from(this.selectedTeeth).sort((a, b) => a - b);
  }

  add(): void {
    this.errorMessage = '';

    if (!this.selectedTeeth.size || !this.problemType) {
      this.errorMessage = 'Please select teeth and problem type.';
      return;
    }

    if (this.problemType === 'IMPACTION' && !this.impactionType) {
      this.errorMessage = 'Please select type of impaction.';
      return;
    }

    const problem: ClinicalProblem = {
      problemType: this.problemType,
      teeth: this.sortedSelectedTeeth(),
      notes: this.notes.trim() || null,
      impactionType: this.problemType === 'IMPACTION'
        ? this.impactionType as ImpactionType
        : null,
      implantDetail: this.problemType === 'IMPLANTS'
        ? {
            implantType: this.implantType as ImplantType || null,
            implantWidthMm: this.numberOrNull(this.implantWidthMm),
            implantLengthMm: this.numberOrNull(this.implantLengthMm),
            implantCompany: this.implantCompany.trim() || null,
            healingCapPlacementDate: this.healingCapPlacementDate || null,
            abutmentPlacementDate: this.abutmentPlacementDate || null,
            crownPlacementDate: this.crownPlacementDate || null
          }
        : null,
      rootCanalLengths: this.problemType === 'ROOT_CANAL'
        ? this.buildRootCanalLengths()
        : []
    };

    this.problemAdd.emit(problem);
    this.reset();
    this.close.emit();
  }

  cancel(): void {
    this.reset();
    this.close.emit();
  }

  private buildRootCanalLengths(): RootCanalLength[] {
    const lengths: RootCanalLength[] = [];

    this.sortedSelectedTeeth().forEach(tooth => {
      this.ensureRootCanalDraft(tooth);

      Object.entries(this.rootCanalDrafts[tooth] ?? {})
        .forEach(([canalName, draft]) => {
          const lengthMm = this.numberOrNull(draft.lengthMm);
          const notes = draft.notes.trim() || null;

          if (lengthMm !== null || notes) {
            lengths.push({
              toothNumber: tooth,
              canalName,
              lengthMm,
              notes
            });
          }
        });
    });

    return lengths;
  }

  private ensureRootCanalDraft(tooth: number): void {
    if (this.problemType !== 'ROOT_CANAL') {
      return;
    }

    if (!this.rootCanalDrafts[tooth]) {
      this.rootCanalDrafts[tooth] = {};
    }

    [...this.canalsForTooth(tooth), 'Others'].forEach(canal => {
      this.rootCanalDrafts[tooth][canal] ??= {
        lengthMm: '',
        notes: ''
      };
    });
  }

  private numberOrNull(value: string): number | null {
    const trimmed = value.trim();

    if (!trimmed) {
      return null;
    }

    const amount = Number(trimmed);
    return Number.isFinite(amount) && amount > 0
      ? amount
      : null;
  }

  private reset(): void {
    this.selectedTeeth.clear();
    this.problemType = '';
    this.notes = '';
    this.errorMessage = '';
    this.impactionType = '';
    this.implantType = '';
    this.implantWidthMm = '';
    this.implantLengthMm = '';
    this.implantCompany = '';
    this.healingCapPlacementDate = '';
    this.abutmentPlacementDate = '';
    this.crownPlacementDate = '';
    this.rootCanalDrafts = {};
  }

  private populate(problem: ClinicalProblem): void {
    this.selectedTeeth = new Set(problem.teeth);
    this.problemType = problem.problemType;
    this.notes = problem.notes ?? '';
    this.impactionType = problem.impactionType ?? '';
    this.implantType = problem.implantDetail?.implantType ?? '';
    this.implantWidthMm = problem.implantDetail?.implantWidthMm?.toString() ?? '';
    this.implantLengthMm = problem.implantDetail?.implantLengthMm?.toString() ?? '';
    this.implantCompany = problem.implantDetail?.implantCompany ?? '';
    this.healingCapPlacementDate = problem.implantDetail?.healingCapPlacementDate ?? '';
    this.abutmentPlacementDate = problem.implantDetail?.abutmentPlacementDate ?? '';
    this.crownPlacementDate = problem.implantDetail?.crownPlacementDate ?? '';
    this.rootCanalDrafts = {};

    if (problem.problemType === 'ROOT_CANAL') {
      problem.teeth.forEach(tooth => this.ensureRootCanalDraft(tooth));
      problem.rootCanalLengths.forEach(root => {
        this.ensureRootCanalDraft(root.toothNumber);
        this.rootCanalDrafts[root.toothNumber][root.canalName] ??= {
          lengthMm: '',
          notes: ''
        };
        this.rootCanalDrafts[root.toothNumber][root.canalName].lengthMm =
          root.lengthMm?.toString() ?? '';
        this.rootCanalDrafts[root.toothNumber][root.canalName].notes =
          root.notes ?? '';
      });
    }
  }
}
