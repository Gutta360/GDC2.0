import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  FollowUpContext,
  FollowUpCreateRequest,
  FollowUpResponse,
  Medicine,
  PatientHealthSnapshot,
  TreatmentCreateRequest,
  TreatmentResponse
} from '../models/treatment.model';

@Injectable({
  providedIn: 'root'
})
export class TreatmentService {

  private readonly treatmentApiUrl = '/api/v1/treatments';
  private readonly followUpApiUrl = '/api/v1/follow-ups';
  private readonly medicineApiUrl = '/api/v1/medicines';
  private readonly appointmentApiUrl = '/api/v1/appointments';

  constructor(
    private http: HttpClient
  ) {}

  getMedicines(): Observable<Medicine[]> {
    return this.http.get<Medicine[]>(this.medicineApiUrl);
  }

  getLatestHealthSnapshot(
    patientId: string
  ): Observable<PatientHealthSnapshot> {
    return this.http.get<PatientHealthSnapshot>(
      `${this.appointmentApiUrl}/patients/${patientId}/latest`
    );
  }

  createTreatment(
    request: TreatmentCreateRequest,
    scans: File[]
  ): Observable<TreatmentResponse> {
    return this.http.post<TreatmentResponse>(
      this.treatmentApiUrl,
      this.toFormData(request, scans)
    );
  }

  getLatestTreatment(
    patientId: string
  ): Observable<TreatmentResponse> {
    return this.http.get<TreatmentResponse>(
      `${this.treatmentApiUrl}/patients/${patientId}/latest`
    );
  }

  getTreatments(
    patientId: string
  ): Observable<TreatmentResponse[]> {
    return this.http.get<TreatmentResponse[]>(
      this.treatmentApiUrl,
      { params: { patientId } }
    );
  }

  getFollowUps(
    patientId: string
  ): Observable<FollowUpResponse[]> {
    return this.http.get<FollowUpResponse[]>(
      this.followUpApiUrl,
      { params: { patientId } }
    );
  }

  getFollowUpContext(
    patientId: string
  ): Observable<FollowUpContext> {
    return this.http.get<FollowUpContext>(
      `${this.treatmentApiUrl}/patients/${patientId}/follow-up-context`
    );
  }

  scanImageUrl(scanId: number): string {
    return `${this.treatmentApiUrl}/scans/${scanId}`;
  }

  createFollowUp(
    request: FollowUpCreateRequest,
    scans: File[]
  ): Observable<FollowUpResponse> {
    return this.http.post<FollowUpResponse>(
      this.followUpApiUrl,
      this.toFormData(request, scans)
    );
  }

  private toFormData(
    request: object,
    scans: File[]
  ): FormData {
    const formData = new FormData();

    formData.append(
      'request',
      new Blob(
        [JSON.stringify(request)],
        { type: 'application/json' }
      )
    );

    scans.forEach(scan => {
      formData.append(
        'scans',
        scan,
        scan.name
      );
    });

    return formData;
  }
}
