import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AppointmentAvailability,
  AppointmentCalendarDay,
  AppointmentDay,
  AppointmentRequest,
  DoctorBusyRequest,
  DoctorBusySlot
} from '../models/appointment.model';

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {

  private readonly appointmentApiUrl =
    '/api/v1/appointments';

  private readonly doctorBusyApiUrl =
    '/api/v1/doctor-busy';

  constructor(
    private http: HttpClient
  ) {}

  getCalendar(
    from: string,
    to: string
  ): Observable<AppointmentCalendarDay[]> {

    const params = new HttpParams()
      .set('from', from)
      .set('to', to);

    return this.http.get<AppointmentCalendarDay[]>(
      `${this.appointmentApiUrl}/calendar`,
      { params }
    );
  }

  getDay(
    date: string
  ): Observable<AppointmentDay> {

    const params = new HttpParams()
      .set('date', date);

    return this.http.get<AppointmentDay>(
      `${this.appointmentApiUrl}/day`,
      { params }
    );
  }

  getAvailability(
    date: string
  ): Observable<AppointmentAvailability> {

    const params = new HttpParams()
      .set('date', date);

    return this.http.get<AppointmentAvailability>(
      `${this.appointmentApiUrl}/availability`,
      { params }
    );
  }

  createAppointment(
    request: AppointmentRequest
  ): Observable<unknown> {

    return this.http.post(
      this.appointmentApiUrl,
      request
    );
  }

  createDoctorBusy(
    request: DoctorBusyRequest
  ): Observable<DoctorBusySlot> {

    return this.http.post<DoctorBusySlot>(
      this.doctorBusyApiUrl,
      request
    );
  }

  deleteDoctorBusy(
    id: number
  ): Observable<void> {

    return this.http.delete<void>(
      `${this.doctorBusyApiUrl}/${id}`
    );
  }
}
