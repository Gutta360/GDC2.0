export type AppointmentType =
  | 'NEW'
  | 'FOLLOW_UP';

export interface AppointmentCalendarDay {
  date: string;
  newAppointments: number;
  followUpAppointments: number;
  doctorBusySlots: number;
}

export interface AppointmentDay {
  date: string;
  appointments: AppointmentDayItem[];
  doctorBusySlots: DoctorBusySlot[];
}

export interface AppointmentDayItem {
  appointmentId: string;
  appointmentTime: string;
  appointmentType: AppointmentType;
  patientId: string;
  patientName: string;
  patientMobile: string;
  notes: string | null;
}

export interface DoctorBusySlot {
  id: number;
  time: string;
  notes: string | null;
}

export interface AppointmentAvailability {
  date: string;
  slots: AppointmentSlot[];
}

export interface AppointmentSlot {
  time: string;
  available: boolean;
  occupiedBy: string | null;
}

export interface AppointmentRequest {
  patientId: string;
  appointmentDate: string;
  appointmentTime: string;
  appointmentType: AppointmentType;
  notes: string | null;
  systolicBp: number | null;
  diastolicBp: number | null;
  heartRate: number | null;
  breathingRate: number | null;
  heightCm: number | null;
  weightKg: number | null;
  fbs: number | null;
  rbs: number | null;
  hasDiabetes: boolean;
  hasHypertension: boolean;
  hasHeartDisease: boolean;
  hasAsthma: boolean;
  hasKidneyDisease: boolean;
  hasLiverDisease: boolean;
  hasThyroidDisorder: boolean;
  hasBleedingDisorders: boolean;
  hasNeurologicalIssues: boolean;
  hasDrugAllergy: boolean;
  hasFoodAllergy: boolean;
  hasLatexAllergy: boolean;
  otherAllergyNotes: string | null;
  pastSurgicalHistory: string | null;
  hasRootCanal: boolean;
  hasImplants: boolean;
  hasCrownsOrBridges: boolean;
  hasBraces: boolean;
  hasDentures: boolean;
  dentalComplicationNotes: string | null;
  consentGiven: boolean;
  consentReference: string | null;
}

export interface DoctorBusyRequest {
  date: string;
  time: string;
  notes: string | null;
}
