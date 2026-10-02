import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Medicine,
  MedicineStockRequest,
  PendingPrescriptionItem,
  PharmacyPaymentCreateRequest,
  PharmacyPaymentResponse
} from '../models/pharmacy.model';

@Injectable({
  providedIn: 'root'
})
export class PharmacyService {

  private readonly medicineApiUrl = '/api/v1/medicines';
  private readonly pharmacyApiUrl = '/api/v1/pharmacy';

  constructor(
    private http: HttpClient
  ) {}

  getMedicines(): Observable<Medicine[]> {
    return this.http.get<Medicine[]>(this.medicineApiUrl);
  }

  getStockMedicines(): Observable<Medicine[]> {
    return this.http.get<Medicine[]>(`${this.medicineApiUrl}/stock`);
  }

  createMedicine(request: MedicineStockRequest): Observable<Medicine> {
    return this.http.post<Medicine>(this.medicineApiUrl, request);
  }

  updateMedicine(
    medicineId: string,
    request: MedicineStockRequest
  ): Observable<Medicine> {
    return this.http.put<Medicine>(`${this.medicineApiUrl}/${medicineId}`, request);
  }

  deleteMedicine(medicineId: string): Observable<void> {
    return this.http.delete<void>(`${this.medicineApiUrl}/${medicineId}`);
  }

  getPendingPrescriptions(patientId: string): Observable<PendingPrescriptionItem[]> {
    return this.http.get<PendingPrescriptionItem[]>(
      `${this.pharmacyApiUrl}/pending-prescriptions`,
      { params: { patientId } }
    );
  }

  createPayment(request: PharmacyPaymentCreateRequest): Observable<PharmacyPaymentResponse> {
    return this.http.post<PharmacyPaymentResponse>(
      `${this.pharmacyApiUrl}/payments`,
      request
    );
  }
}
