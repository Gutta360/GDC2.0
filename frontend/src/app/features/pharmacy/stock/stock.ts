import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { MedicineStockModal } from '../components/stock-modal/stock-modal';
import {
  Medicine,
  MedicineStockRequest
} from '../models/pharmacy.model';
import { PharmacyService } from '../services/pharmacy.service';

@Component({
  selector: 'app-medicine-stock',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MedicineStockModal
  ],
  templateUrl: './stock.html',
  styleUrl: './stock.scss'
})
export class MedicineStock implements OnInit, OnDestroy {

  medicines: Medicine[] = [];
  search = '';
  loading = false;
  saving = false;
  modalOpen = false;
  selectedMedicine: Medicine | null = null;
  snackbarMessage = '';
  snackbarType: 'success' | 'error' = 'success';

  private snackbarTimeout: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private pharmacyService: PharmacyService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadMedicines();
  }

  ngOnDestroy(): void {
    if (this.snackbarTimeout) {
      clearTimeout(this.snackbarTimeout);
    }
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

  add(): void {
    this.selectedMedicine = null;
    this.modalOpen = true;
  }

  edit(medicine: Medicine): void {
    this.selectedMedicine = medicine;
    this.modalOpen = true;
  }

  save(request: MedicineStockRequest): void {
    this.saving = true;
    const save$ = this.selectedMedicine
      ? this.pharmacyService.updateMedicine(this.selectedMedicine.medicineId, request)
      : this.pharmacyService.createMedicine(request);

    save$
      .pipe(finalize(() => {
        this.saving = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: () => {
          this.modalOpen = false;
          this.selectedMedicine = null;
          this.loadMedicines();
          this.showSnackbar('Stock Saved Successfully', 'success');
        },
        error: error => {
          this.showSnackbar(error?.error?.message ?? 'Could not save stock.', 'error');
        }
      });
  }

  delete(): void {
    if (!this.selectedMedicine || this.saving) {
      return;
    }

    this.saving = true;

    this.pharmacyService
      .deleteMedicine(this.selectedMedicine.medicineId)
      .pipe(finalize(() => {
        this.saving = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: () => {
          this.modalOpen = false;
          this.selectedMedicine = null;
          this.loadMedicines();
          this.showSnackbar('Stock Deleted Successfully', 'success');
        },
        error: error => {
          this.showSnackbar(error?.error?.message ?? 'Could not delete stock.', 'error');
        }
      });
  }

  private loadMedicines(): void {
    this.loading = true;

    this.pharmacyService
      .getStockMedicines()
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: medicines => {
          this.medicines = medicines;
        },
        error: () => {
          this.medicines = [];
          this.showSnackbar('Could not load medicine stock.', 'error');
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
  }
}
