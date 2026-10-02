import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Medicine, PrescriptionItem } from '../../models/treatment.model';
import { TreatmentService } from '../../services/treatment.service';

@Component({
  selector: 'app-medicine-selector',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './medicine-selector.html',
  styleUrl: './medicine-selector.scss'
})
export class MedicineSelector implements OnInit {

  @Input() open = false;
  @Input() cart: PrescriptionItem[] = [];

  @Output() close = new EventEmitter<void>();
  @Output() cartChange = new EventEmitter<PrescriptionItem[]>();

  medicines: Medicine[] = [];
  search = '';
  loading = false;
  errorMessage = '';

  constructor(
    private treatmentService: TreatmentService
  ) {}

  ngOnInit(): void {
    this.loading = true;

    this.treatmentService
      .getMedicines()
      .subscribe({
        next: medicines => {
          this.medicines = medicines;
          this.loading = false;
        },
        error: () => {
          this.medicines = [];
          this.loading = false;
          this.errorMessage = 'Could not load medicines.';
        }
      });
  }

  get filteredMedicines(): Medicine[] {
    const query = this.search.trim().toLowerCase();

    if (!query) {
      return this.medicines;
    }

    return this.medicines.filter(medicine =>
      medicine.medicineName.toLowerCase().includes(query)
    );
  }

  addMedicine(medicine: Medicine): void {
    this.errorMessage = '';

    const existing = this.cart.find(
      item => item.medicineId === medicine.medicineId
    );

    const currentQuantity = existing?.quantity ?? 0;

    if (currentQuantity >= medicine.availableQuantity) {
      this.errorMessage =
        `Only ${medicine.availableQuantity} quantity available for ${medicine.medicineName}`;
      return;
    }

    const nextCart = existing
      ? this.cart.map(item =>
          item.medicineId === medicine.medicineId
            ? { ...item, quantity: item.quantity + 1 }
            : item
        )
      : [
          ...this.cart,
          {
            medicineId: medicine.medicineId,
            medicineName: medicine.medicineName,
            availableQuantity: medicine.availableQuantity,
            quantity: 1
          }
        ];

    this.emitCart(nextCart);
  }

  decrement(item: PrescriptionItem): void {
    if (item.quantity <= 1) {
      return;
    }

    this.emitCart(
      this.cart.map(cartItem =>
        cartItem.medicineId === item.medicineId
          ? { ...cartItem, quantity: cartItem.quantity - 1 }
          : cartItem
      )
    );
  }

  increment(item: PrescriptionItem): void {
    if (item.quantity >= item.availableQuantity) {
      this.errorMessage =
        `Only ${item.availableQuantity} quantity available for ${item.medicineName}`;
      return;
    }

    this.emitCart(
      this.cart.map(cartItem =>
        cartItem.medicineId === item.medicineId
          ? { ...cartItem, quantity: cartItem.quantity + 1 }
          : cartItem
      )
    );
  }

  remove(item: PrescriptionItem): void {
    this.emitCart(
      this.cart.filter(cartItem => cartItem.medicineId !== item.medicineId)
    );
  }

  done(): void {
    this.errorMessage = '';
    this.close.emit();
  }

  private emitCart(cart: PrescriptionItem[]): void {
    this.cart = cart;
    this.cartChange.emit(cart);
  }
}
