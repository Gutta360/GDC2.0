import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  inject,
  OnChanges,
  Output,
  SimpleChanges
} from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import {
  Medicine,
  MedicineStockRequest
} from '../../models/pharmacy.model';
import { IntegerInputDirective } from '../../../../shared/directives/integer-input.directive';

export function clinicToday(): string {
  const today = new Date();
  const year = today.getFullYear();
  const month = `${today.getMonth() + 1}`.padStart(2, '0');
  const day = `${today.getDate()}`.padStart(2, '0');

  return `${year}-${month}-${day}`;
}

export function isPastExpiryDate(
  expiryDate: string,
  today = clinicToday()
): boolean {
  return !!expiryDate && expiryDate < today;
}

@Component({
  selector: 'app-medicine-stock-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    IntegerInputDirective
  ],
  templateUrl: './stock-modal.html',
  styleUrl: './stock-modal.scss'
})
export class MedicineStockModal implements OnChanges {

  private fb = inject(FormBuilder);

  @Input() open = false;
  @Input() medicine: Medicine | null = null;
  @Input() saving = false;

  @Output() close = new EventEmitter<void>();
  @Output() saveStock = new EventEmitter<MedicineStockRequest>();
  @Output() deleteStock = new EventEmitter<void>();

  confirmDelete = false;
  readonly today = clinicToday();

  form = this.fb.group({
    medicineName: ['', [
      Validators.required,
      Validators.maxLength(150)
    ]],
    quantity: ['', [
      Validators.required,
      Validators.pattern(/^\d+$/),
      Validators.max(1_000_000)
    ]],
    expiryDate: ['', Validators.required]
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['open'] && this.open) {
      this.confirmDelete = false;
      this.form.reset({
        medicineName: this.medicine?.medicineName ?? '',
        quantity: this.medicine?.availableQuantity?.toString() ?? '',
        expiryDate: this.medicine?.expiryDate ?? ''
      });
    }
  }

  get title(): string {
    return this.medicine ? 'Edit Stock' : 'Add Stock';
  }

  get pastExpiryInvalid(): boolean {
    return !this.medicine && isPastExpiryDate(this.form.controls.expiryDate.value ?? '', this.today);
  }

  submit(): void {
    if (this.form.invalid || this.pastExpiryInvalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    this.saveStock.emit({
      medicineName: (value.medicineName ?? '').trim(),
      quantity: Number(value.quantity),
      expiryDate: value.expiryDate ?? ''
    });
  }

  requestDelete(): void {
    this.confirmDelete = true;
  }
}
