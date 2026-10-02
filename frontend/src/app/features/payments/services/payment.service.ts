import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  OutstandingTreatmentPayment,
  PaymentCreateRequest,
  PaymentResponse
} from '../models/payment.model';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  private readonly paymentApiUrl = '/api/v1/payments';

  constructor(
    private http: HttpClient
  ) {}

  getOutstandingTreatments(patientId: string): Observable<OutstandingTreatmentPayment[]> {
    return this.http.get<OutstandingTreatmentPayment[]>(
      `${this.paymentApiUrl}/outstanding`,
      { params: { patientId } }
    );
  }

  createPayment(request: PaymentCreateRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(this.paymentApiUrl, request);
  }

  getPayments(patientId: string): Observable<PaymentResponse[]> {
    return this.http.get<PaymentResponse[]>(
      this.paymentApiUrl,
      { params: { patientId } }
    );
  }
}
