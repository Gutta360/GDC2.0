import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  OnInit
} from '@angular/core';
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
  snackbarMessage = '';
  snackbarType: 'success' | 'error' = 'error';

  selectedDate: Date | null = null;
  selectedDateIso = '';
  dayDetails: AppointmentDay | null = null;

  createOpen = false;
  busyOpen = false;
  patientSearch = '';
  patientDropdownOpen = false;
  allPatients: PatientSummary[] = [];
  filteredPatients: PatientSummary[] = [];
  searchingPatients = false;
  availability: AppointmentSlot[] = [];

  appointmentForm!: FormGroup;
  busyForm!: FormGroup;
  private snackbarTimeout: ReturnType<typeof setTimeout> | null = null;

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
    private patientService: PatientService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {

    this.focusedMonth = new Date(
      this.focusedMonth.getFullYear(),
      this.focusedMonth.getMonth(),
      1
    );

    this.buildForms();
    this.loadCalendar();
    this.loadPatients();
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

  get selectedPatientId(): string {
    return this.appointmentForm?.value?.patientId ?? '';
  }

  get selectedPatientLabel(): string {

    if (!this.selectedPatientId) {
      return 'Select patient';
    }

    const selected =
      this.allPatients.find(patient =>
        patient.patientId === this.selectedPatientId
      );

    if (!selected) {
      return this.selectedPatientId;
    }

    return this.patientLabel(selected);
  }

  get patientSearchResults(): PatientSummary[] {

    const query =
      this.patientSearch
        .trim()
        .toLowerCase();

    const patients = query
      ? this.filteredPatients
      : this.allPatients;

    return patients.slice(0, 20);
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
    this.loadDayDetails(cell.isoDate);
    this.loadAvailability(cell.isoDate);
  }

  closeDay(): void {
    this.dayDetails = null;
    this.selectedDate = null;
    this.selectedDateIso = '';
    this.loadingDay = false;
    this.createOpen = false;
    this.busyOpen = false;
    this.patientDropdownOpen = false;
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
    this.filteredPatients = [];
    this.patientDropdownOpen = false;
    this.appointmentForm.patchValue({
      patientId: ''
    });
    this.ensurePatientsLoaded();
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

  togglePatientDropdown(): void {

    this.patientDropdownOpen =
      !this.patientDropdownOpen;

    if (this.patientDropdownOpen) {
      this.patientSearch = '';
      this.filteredPatients = [];
      this.ensurePatientsLoaded();
    }
  }

  onPatientSearchChange(
    value: string
  ): void {

    this.patientSearch = value;
    this.searchPatients();
  }

  searchPatients(): void {

    const query =
      this.patientSearch
        .trim()
        .toLowerCase();

    this.ensurePatientsLoaded();

    const selectedPatientId =
      this.appointmentForm.value.patientId;

    if (selectedPatientId) {
      const selectedPatient =
        this.allPatients.find(patient =>
          patient.patientId === selectedPatientId
        );

      if (!selectedPatient || this.patientLabel(selectedPatient) !== this.patientSearch) {
        this.appointmentForm.patchValue({
          patientId: ''
        });
      }
    }

    if (!query) {
      this.filteredPatients = [];
      this.cdr.detectChanges();
      return;
    }

    this.filteredPatients =
      this.allPatients
        .filter(patient => {

          const fullName =
            patient.fullName ||
            `${patient.firstName} ${patient.lastName ?? ''}`.trim();

          return (
            patient.patientId.toLowerCase().includes(query) ||
            fullName.toLowerCase().includes(query) ||
            (patient.mobile ?? '').includes(query)
          );
        })
        .slice(0, 20);

    this.cdr.detectChanges();
  }

  selectPatient(
    patient: PatientSummary
  ): void {

    this.appointmentForm.patchValue({
      patientId: patient.patientId
    });

    this.patientSearch =
      this.patientLabel(patient);

    this.filteredPatients = [];
    this.patientDropdownOpen = false;
    this.patientSearch = '';
    this.cdr.detectChanges();
  }

  patientLabel(
    patient: PatientSummary
  ): string {

    const fullName =
      patient.fullName ||
      `${patient.firstName} ${patient.lastName ?? ''}`.trim();

    return `${patient.patientId}  ${fullName}`;
  }

  submitAppointment(): void {

    if (this.appointmentForm.invalid || !this.selectedDateIso || this.saving) {
      this.appointmentForm.markAllAsTouched();
      this.showSnackbar(
        'Please select a patient, choose a time, and complete the required fields.',
        'error'
      );
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
          const bookedMessage =
            this.appointmentBookedMessage(
              request.appointmentDate,
              request.appointmentTime
            );

          this.successMessage = bookedMessage;
          this.showSnackbar(
            bookedMessage,
            'success'
          );
          this.createOpen = false;
          this.refreshCurrentViews();
        },
        error: error => {
          const message =
            this.extractErrorMessage(
              error,
              'Could not create appointment.'
            );

          this.errorMessage = message;
          this.showSnackbar(message, 'error');
          this.loadAvailability();
        }
      });
  }

  submitDoctorBusy(): void {

    if (this.busyForm.invalid || !this.selectedDateIso || this.saving) {
      this.busyForm.markAllAsTouched();
      this.showSnackbar(
        'Please choose a time before saving doctor busy hours.',
        'error'
      );
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
          this.showSnackbar(
            'Doctor busy time saved.',
            'success'
          );
          this.busyOpen = false;
          this.refreshCurrentViews();
        },
        error: error => {
          const message =
            this.extractErrorMessage(
              error,
              'Could not save busy time.'
            );

          this.errorMessage = message;
          this.showSnackbar(message, 'error');
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
        this.cdr.detectChanges();
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
          this.cdr.detectChanges();
        },
        error: error => {
          this.errorMessage =
            error?.error?.message ?? 'Could not load appointment calendar.';
          this.cdr.detectChanges();
        }
      });
  }

  private loadDayDetails(
    dateIso: string = this.selectedDateIso
  ): void {

    if (!dateIso) {
      return;
    }

    const requestedDateIso = dateIso;

    this.loadingDay = true;
    this.dayDetails = null;
    this.errorMessage = '';
    this.successMessage = '';

    this.appointmentService
      .getDay(requestedDateIso)
      .pipe(finalize(() => {
        if (this.selectedDateIso === requestedDateIso) {
          this.loadingDay = false;
          this.cdr.detectChanges();
        }
      }))
      .subscribe({
        next: day => {
          if (this.selectedDateIso !== requestedDateIso) {
            return;
          }

          this.dayDetails = {
            ...day,
            appointments: day.appointments ?? [],
            doctorBusySlots: day.doctorBusySlots ?? []
          };
          this.cdr.detectChanges();
        },
        error: error => {
          if (this.selectedDateIso !== requestedDateIso) {
            return;
          }

          this.errorMessage =
            error?.error?.message ?? 'Could not load day details.';
          this.cdr.detectChanges();
        }
      });
  }

  private loadAvailability(
    dateIso: string = this.selectedDateIso
  ): void {

    if (!dateIso) {
      return;
    }

    const requestedDateIso = dateIso;

    this.appointmentService
      .getAvailability(requestedDateIso)
      .subscribe({
        next: availability => {
          if (this.selectedDateIso !== requestedDateIso) {
            return;
          }

          this.availability = availability.slots;
          this.cdr.detectChanges();
        },
        error: () => {
          if (this.selectedDateIso !== requestedDateIso) {
            return;
          }

          this.availability = [];
          this.cdr.detectChanges();
        }
      });
  }

  private refreshCurrentViews(): void {
    this.loadCalendar();
    this.loadDayDetails();
    this.loadAvailability();
  }

  private loadPatients(): void {

    this.searchingPatients = true;

    this.patientService
      .getAllPatients()
      .pipe(finalize(() => {
        this.searchingPatients = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: patients => {
          this.allPatients = patients.map(patient => ({
            patientId: patient.patientId,
            firstName: patient.firstName,
            lastName: patient.lastName,
            fullName: patient.fullName,
            mobile: patient.mobile
          }));

          if (this.patientSearch.trim()) {
            this.searchPatients();
          }

          this.cdr.detectChanges();
        },
        error: () => {
          this.allPatients = [];
          this.filteredPatients = [];
          this.cdr.detectChanges();
        }
      });
  }

  private ensurePatientsLoaded(): void {

    if (!this.allPatients.length && !this.searchingPatients) {
      this.loadPatients();
    }
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

    this.cdr.detectChanges();
  }

  private extractErrorMessage(
    error: {
      error?: {
        message?: string;
        fieldErrors?: Record<string, string>;
      };
    },
    fallback: string
  ): string {

    const fieldErrors =
      error?.error?.fieldErrors;

    if (fieldErrors && Object.keys(fieldErrors).length) {
      return Object.values(fieldErrors).join(' ');
    }

    return error?.error?.message ?? fallback;
  }

  private appointmentBookedMessage(
    dateIso: string,
    time: string
  ): string {

    const [year, month, day] =
      dateIso.split('-').map(Number);

    const date =
      new Date(
        year,
        month - 1,
        day
      );

    const formattedDate =
      date.toLocaleDateString(
        'en-GB',
        {
          weekday: 'long',
          day: 'numeric',
          month: 'long',
          year: 'numeric'
        }
      );

    return (
      'Appointment is scheduled at ' +
      `${formattedDate} at ${this.formatTime(time)} successfully`
    );
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
