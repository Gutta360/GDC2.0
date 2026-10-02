import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';
import {
  ClinicalProblem,
  impactionTypeLabels,
  implantTypeLabels,
  problemTypeLabels
} from '../../models/treatment.model';

@Component({
  selector: 'app-treatment-problem-list',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './problem-list.html',
  styleUrl: './problem-list.scss'
})
export class TreatmentProblemList {

  @Input() problems: ClinicalProblem[] = [];

  @Output() removeProblem = new EventEmitter<number>();
  @Output() editProblem = new EventEmitter<number>();

  problemLabel(problem: ClinicalProblem): string {
    return problemTypeLabels[problem.problemType];
  }

  details(problem: ClinicalProblem): string[] {
    const details: string[] = [];

    if (problem.impactionType) {
      details.push(`Impaction: ${impactionTypeLabels[problem.impactionType]}`);
    }

    if (problem.implantDetail) {
      const implant = problem.implantDetail;

      if (implant.implantType) {
        details.push(`Implant: ${implantTypeLabels[implant.implantType]}`);
      }

      if (implant.implantWidthMm) {
        details.push(`Width: ${implant.implantWidthMm}mm`);
      }

      if (implant.implantLengthMm) {
        details.push(`Length: ${implant.implantLengthMm}mm`);
      }

      if (implant.implantCompany) {
        details.push(`Company: ${implant.implantCompany}`);
      }
    }

    problem.rootCanalLengths.forEach(root => {
      const value = root.lengthMm
        ? `${root.lengthMm}mm`
        : root.notes;

      if (value) {
        details.push(`Tooth ${root.toothNumber} ${root.canalName}: ${value}`);
      }
    });

    return details;
  }
}
