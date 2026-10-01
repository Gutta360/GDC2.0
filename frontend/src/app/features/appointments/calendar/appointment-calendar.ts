import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import {
  AppointmentCalendarDay,
  AppointmentDay,
  AppointmentRequest,
  AppointmentSlot,
  AppointmentType,
  DoctorBusyRequest
} from '../models/appointment.model';
import { AppointmentService } from '../services/appointment.service';
import { PatientSummary } from '../../patients/models/patient.model';
import { PatientService } from '../../patients/services/patient.service';

interface CalendarCell {
  date: Date;
  isoDate: string;
  day: number;
  inFocusedMonth: boolean;
  isToday: boolean;
  counts?: AppointmentCalendarDay;
}

@Component({
  selector: 'app-appointment-calendar',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './appointment-calendar.html',
  styleUrl: './appointment-calendar.scss'
})
export class AppointmentCalendar implements OnInit {

  focusedMonth = new Date();
  calendarCells: CalendarCell[] = [];
  calendarCounts = new Map<string, AppointmentCalendarDay>();
  weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  loadingCalendar = false;
  loadingDay = false;
  saving = false;
  errorMessage = '';
  successMessage = '';

  selectedDate: Date | null = null;
  selectedDateIso = '';
  dayDetails: AppointmentDay | null = null;

  createOpen = false;
  busyOpen = false;
  patientSearch = '';
  patients: PatientSummary[] = [];
  searchingPatients = false;
  availability: AppointmentSlot[] = [];

  appointmentForm!: FormGroup;
  busyForm!: FormGroup;

  readonly conditionFields = [
    ['hasDiabetes', 'Diabetes'],
    ['hasHypertension', 'Hypertension'],
    ['hasHeartDisease', 'Heart Disease'],
    ['hasAsthma', 'Asthma'],
    ['hasKidneyDisease', 'Kidney Disease'],
    ['hasLiverDisease', 'Liver Disease'],
    ['hasThyroidDisorder', 'Thyroid Disorder'],
    ['hasBleedingDisorders', 'Bleeding Disorders'],
    ['hasNeurologicalIssues', 'Neurological Issues']
  ];

  readonly dentalFields = [
    ['hasRootCanal', 'Root Canal'],
    ['hasImplants', 'Implants'],
    ['hasCrownsOrBridges', 'Crowns / Bridges'],
    ['hasBraces', 'Braces'],
    ['hasDentures', 'Dentures']
  ];

  constructor(
    private fb: FormBuilder,
    private appointmentService: AppointmentService,
    private patientService: PatientService
  ) {}

  ngOnInit(): void {

    this.focusedMonth = new Date(
      this.focusedMonth.getFullYear(),
      this.focusedMonth.getMonth(),
      1
    );

    this.buildForms();
    this.loadCalendar();
  }

  get monthTitle(): string {
    return this.focusedMonth.toLocaleDateString(
      'en-GB',
      {
        month: 'long',
        year: 'numeric'
      }
    );
  }

  get selectedDateTitle(): string {

    if (!this.selectedDate) {
      return '';
    }

    return this.selectedDate.toLocaleDateString(
      'en-GB',
      {
        weekday: 'long',
        day: 'numeric',
        month: 'long',
        year: 'numeric'
      }
    );
  }

  get compactSelectedDateTitle(): string {

    if (!this.selectedDate) {
      return '';
    }

    return this.selectedDate.toLocaleDateString(
      'en-GB',
      {
        weekday: 'long',
        day: 'numeric',
        month: 'short',
        year: 'numeric'
      }
    );
  }

  previousMonth(): void {

    this.focusedMonth = new Date(
      this.focusedMonth.getFullYear(),
      this.focusedMonth.getMonth() - 1,
      1
    );

    this.loadCalendar();
  }

  nextMonth(): void {

    this.focusedMonth = new Date(
      this.focusedMonth.getFullYear(),
      this.focusedMonth.getMonth() + 1,
      1
    );

    this.loadCalendar();
  }

  openDay(
    cell: CalendarCell
  ): void {

    this.selectedDate = cell.date;
    this.selectedDateIso = cell.isoDate;
    this.createOpen = false;
    this.busyOpen = false;
    this.loadDayDetails();
  }

  closeDay(): void {
    this.dayDetails = null;
    this.selectedDate = null;
    this.selectedDateIso = '';
    this.createOpen = false;
    this.busyOpen = false;
  }

  openCreateAppointment(): void {

    this.createOpen = true;
    this.busyOpen = false;
    this.successMessage = '';
    this.errorMessage = '';
    this.appointmentForm.reset({
      appointmentType: 'NEW',
      heartRate: 72,
      breathingRate: 16,
      hasDiabetes: false,
      hasHypertension: false,
      hasHeartDisease: false,
      hasAsthma: false,
      hasKidneyDisease: false,
      hasLiverDisease: false,
      hasThyroidDisorder: false,
      hasBleedingDisorders: false,
      hasNeurologicalIssues: false,
      hasDrugAllergy: false,
      hasFoodAllergy: false,
      hasLatexAllergy: false,
      hasRootCanal: false,
      hasImplants: false,
      hasCrownsOrBridges: false,
      hasBraces: false,
      hasDentures: false,
      consentGiven: false
    });
    this.patientSearch = '';
    this.patients = [];
    this.loadAvailability();
  }

  openDoctorBusy(): void {

    this.busyOpen = true;
    this.createOpen = false;
    this.successMessage = '';
    this.errorMessage = '';
    this.busyForm.reset({
      time: '',
      notes: ''
    });
    this.loadAvailability();
  }

  closeCreate(): void {
    this.createOpen = false;
  }

  closeBusy(): void {
    this.busyOpen = false;
  }

  searchPatients(): void {

    const query =
      this.patientSearch.trim();

    if (query.length < 2) {
      this.patients = [];
      return;
    }

    this.searchingPatients = true;

    this.patientService
      .searchActivePatients(query)
      .pipe(finalize(() => {
        this.searchingPatients = false;
      }))
      .subscribe({
        next: patients => {
          this.patients = patients;
        },
        error: () => {
          this.patients = [];
        }
      });
  }

  selectPatient(
    patient: PatientSummary
  ): void {

    this.appointmentForm.patchValue({
      patientId: patient.patientId
    });

    this.patientSearch =
      `${patient.patientId}  ${patient.fullName}`;

    this.patients = [];
  }

  submitAppointment(): void {

    if (this.appointmentForm.invalid || !this.selectedDateIso || this.saving) {
      this.appointmentForm.markAllAsTouched();
      return;
    }

    const value = this.appointmentForm.getRawValue();

    const request: AppointmentRequest = {
      patientId: value.patientId,
      appointmentDate: this.selectedDateIso,
      appointmentTime: value.appointmentTime,
      appointmentType: value.appointmentType,
      notes: this.nullable(value.notes),
      systolicBp: this.numberOrNull(value.systolicBp),
      diastolicBp: this.numberOrNull(value.diastolicBp),
      heartRate: this.numberOrNull(value.heartRate),
      breathingRate: this.numberOrNull(value.breathingRate),
      heightCm: this.numberOrNull(value.heightCm),
      weightKg: this.numberOrNull(value.weightKg),
      fbs: this.numberOrNull(value.fbs),
      rbs: this.numberOrNull(value.rbs),
      hasDiabetes: value.hasDiabetes,
      hasHypertension: value.hasHypertension,
      hasHeartDisease: value.hasHeartDisease,
      hasAsthma: value.hasAsthma,
      hasKidneyDisease: value.hasKidneyDisease,
      hasLiverDisease: value.hasLiverDisease,
      hasThyroidDisorder: value.hasThyroidDisorder,
      hasBleedingDisorders: value.hasBleedingDisorders,
      hasNeurologicalIssues: value.hasNeurologicalIssues,
      hasDrugAllergy: value.hasDrugAllergy,
      hasFoodAllergy: value.hasFoodAllergy,
      hasLatexAllergy: value.hasLatexAllergy,
      otherAllergyNotes: this.nullable(value.otherAllergyNotes),
      pastSurgicalHistory: this.nullable(value.pastSurgicalHistory),
      hasRootCanal: value.hasRootCanal,
      hasImplants: value.hasImplants,
      hasCrownsOrBridges: value.hasCrownsOrBridges,
      hasBraces: value.hasBraces,
      hasDentures: value.hasDentures,
      dentalComplicationNotes: this.nullable(value.dentalComplicationNotes),
      consentGiven: value.consentGiven,
      consentReference: this.nullable(value.consentReference)
    };

    this.saving = true;
    this.errorMessage = '';

    this.appointmentService
      .createAppointment(request)
      .pipe(finalize(() => {
        this.saving = false;
      }))
      .subscribe({
        next: () => {
          this.successMessage = 'Appointment created.';
          this.createOpen = false;
          this.refreshCurrentViews();
        },
        error: error => {
          this.errorMessage =
            error?.error?.message ?? 'Could not create appointment.';
          this.loadAvailability();
        }
      });
  }

  submitDoctorBusy(): void {

    if (this.busyForm.invalid || !this.selectedDateIso || this.saving) {
      this.busyForm.markAllAsTouched();
      return;
    }

    const request: DoctorBusyRequest = {
      date: this.selectedDateIso,
      time: this.busyForm.value.time,
      notes: this.nullable(this.busyForm.value.notes)
    };

    this.saving = true;
    this.errorMessage = '';

    this.appointmentService
      .createDoctorBusy(request)
      .pipe(finalize(() => {
        this.saving = false;
      }))
      .subscribe({
        next: () => {
          this.successMessage = 'Doctor busy time saved.';
          this.busyOpen = false;
          this.refreshCurrentViews();
        },
        error: error => {
          this.errorMessage =
            error?.error?.message ?? 'Could not save busy time.';
          this.loadAvailability();
        }
      });
  }

  openWhatsAppReminder(
    appointment: {
      patientName: string;
      patientMobile: string;
      appointmentTime: string;
    }
  ): void {

    const confirmed = window.confirm(
      `Open WhatsApp to send reminder to ${appointment.patientName}?`
    );

    if (!confirmed || !this.selectedDate) {
      return;
    }

    const mobile = this.normalizeMobileForWhatsapp(
      appointment.patientMobile
    );
    const date = this.selectedDate.toLocaleDateString(
      'en-GB',
      {
        day: 'numeric',
        month: 'long',
        year: 'numeric'
      }
    );
    const message =
      `Dear ${appointment.patientName},\n\n` +
      'This is a gentle reminder of your appointment with Dr. Ramesh.\n\n' +
      `Date : ${date}\n` +
      `Time : ${this.formatTime(appointment.appointmentTime)}\n\n` +
      'Please be on time.\n\n' +
      'Thank you,\n' +
      'Global Dental Clinic';

    window.open(
      `https://wa.me/${mobile}?text=${encodeURIComponent(message)}`,
      '_blank',
      'noopener'
    );
  }

  formatTime(
    value: string
  ): string {
    return value?.slice(0, 5) ?? '';
  }

  appointmentTypeLabel(
    value: AppointmentType
  ): string {
    return value === 'FOLLOW_UP'
      ? 'Follow-up'
      : 'New';
  }

  trackCell(
    _index: number,
    cell: CalendarCell
  ): string {
    return cell.isoDate;
  }

  private buildForms(): void {

    this.appointmentForm = this.fb.group({
      patientId: ['', Validators.required],
      appointmentTime: ['', Validators.required],
      appointmentType: ['NEW', Validators.required],
      notes: [''],
      systolicBp: [''],
      diastolicBp: [''],
      heartRate: [72],
      breathingRate: [16],
      heightCm: [''],
      weightKg: [''],
      fbs: [''],
      rbs: [''],
      hasDiabetes: [false],
      hasHypertension: [false],
      hasHeartDisease: [false],
      hasAsthma: [false],
      hasKidneyDisease: [false],
      hasLiverDisease: [false],
      hasThyroidDisorder: [false],
      hasBleedingDisorders: [false],
      hasNeurologicalIssues: [false],
      hasDrugAllergy: [false],
      hasFoodAllergy: [false],
      hasLatexAllergy: [false],
      otherAllergyNotes: [''],
      pastSurgicalHistory: [''],
      hasRootCanal: [false],
      hasImplants: [false],
      hasCrownsOrBridges: [false],
      hasBraces: [false],
      hasDentures: [false],
      dentalComplicationNotes: [''],
      consentGiven: [false],
      consentReference: ['']
    });

    this.busyForm = this.fb.group({
      time: ['', Validators.required],
      notes: ['']
    });
  }

  private loadCalendar(): void {

    this.calendarCells = this.buildCalendarCells();
    const from = this.calendarCells[0].isoDate;
    const to = this.calendarCells[this.calendarCells.length - 1].isoDate;

    this.loadingCalendar = true;
    this.errorMessage = '';

    this.appointmentService
      .getCalendar(from, to)
      .pipe(finalize(() => {
        this.loadingCalendar = false;
      }))
      .subscribe({
        next: days => {
          this.calendarCounts = new Map(
            days.map(day => [day.date, day])
          );
          this.calendarCells = this.calendarCells.map(cell => ({
            ...cell,
            counts: this.calendarCounts.get(cell.isoDate)
          }));
        },
        error: error => {
          this.errorMessage =
            error?.error?.message ?? 'Could not load appointment calendar.';
        }
      });
  }

  private loadDayDetails(): void {

    if (!this.selectedDateIso) {
      return;
    }

    this.loadingDay = true;
    this.dayDetails = null;
    this.errorMessage = '';
    this.successMessage = '';

    this.appointmentService
      .getDay(this.selectedDateIso)
      .pipe(finalize(() => {
        this.loadingDay = false;
      }))
      .subscribe({
        next: day => {
          this.dayDetails = day;
        },
        error: error => {
          this.errorMessage =
            error?.error?.message ?? 'Could not load day details.';
        }
      });
  }

  private loadAvailability(): void {

    if (!this.selectedDateIso) {
      return;
    }

    this.appointmentService
      .getAvailability(this.selectedDateIso)
      .subscribe({
        next: availability => {
          this.availability = availability.slots;
        },
        error: () => {
          this.availability = [];
        }
      });
  }

  private refreshCurrentViews(): void {
    this.loadCalendar();
    this.loadDayDetails();
    this.loadAvailability();
  }

  private buildCalendarCells(): CalendarCell[] {

    const firstOfMonth = new Date(
      this.focusedMonth.getFullYear(),
      this.focusedMonth.getMonth(),
      1
    );
    const lastOfMonth = new Date(
      this.focusedMonth.getFullYear(),
      this.focusedMonth.getMonth() + 1,
      0
    );
    const start = new Date(firstOfMonth);
    start.setDate(firstOfMonth.getDate() - firstOfMonth.getDay());

    const daysNeeded =
      firstOfMonth.getDay() + lastOfMonth.getDate() <= 35
        ? 35
        : 42;

    const todayIso = this.toIsoDate(new Date());

    return Array.from(
      { length: daysNeeded },
      (_, index) => {
        const date = new Date(start);
        date.setDate(start.getDate() + index);

        return {
          date,
          isoDate: this.toIsoDate(date),
          day: date.getDate(),
          inFocusedMonth:
            date.getMonth() === this.focusedMonth.getMonth(),
          isToday:
            this.toIsoDate(date) === todayIso,
          counts:
            this.calendarCounts.get(this.toIsoDate(date))
        };
      }
    );
  }

  private toIsoDate(
    date: Date
  ): string {

    const pad = (value: number): string =>
      value.toString().padStart(2, '0');

    return (
      `${date.getFullYear()}-` +
      `${pad(date.getMonth() + 1)}-` +
      `${pad(date.getDate())}`
    );
  }

  private nullable(
    value: unknown
  ): string | null {

    const normalized =
      String(value ?? '').trim();

    return normalized
      ? normalized
      : null;
  }

  private numberOrNull(
    value: unknown
  ): number | null {

    if (value === null || value === undefined || value === '') {
      return null;
    }

    return Number(value);
  }

  private normalizeMobileForWhatsapp(
    value: string
  ): string {

    const digits =
      String(value ?? '').replace(/\D/g, '');

    if (digits.length === 10) {
      return `91${digits}`;
    }

    return digits;
  }
}
