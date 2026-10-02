export type TreatmentType =
  | 'ADVISED'
  | 'DESIRED';

export type ProblemType =
  | 'ROOT_CANAL'
  | 'IMPLANTS'
  | 'IMPACTION'
  | 'CROWNS_BRIDGES'
  | 'BRACES'
  | 'DENTURES'
  | 'EXTRACTION'
  | 'FILLING'
  | 'SCALING'
  | 'CERVICAL_ABRASION'
  | 'GENERALIZED_GINGIVITIS';

export type ImplantType =
  | 'BASAL'
  | 'CONVENTIONAL';

export type ImpactionType =
  | 'MESIO_ANGULAR'
  | 'DISTO_ANGULAR'
  | 'VERTICAL'
  | 'HORIZONTAL';

export interface Medicine {
  medicineId: string;
  medicineName: string;
  availableQuantity: number;
  expiryDate: string;
}

export interface PrescriptionItemRequest {
  medicineId: string;
  quantity: number;
}

export interface PrescriptionItem extends PrescriptionItemRequest {
  medicineName: string;
  availableQuantity: number;
}

export interface RootCanalLength {
  toothNumber: number;
  canalName: string;
  lengthMm: number | null;
  notes: string | null;
}

export interface ImplantDetail {
  implantType: ImplantType | null;
  implantWidthMm: number | null;
  implantLengthMm: number | null;
  implantCompany: string | null;
  healingCapPlacementDate: string | null;
  abutmentPlacementDate: string | null;
  crownPlacementDate: string | null;
}

export interface ClinicalProblem {
  problemType: ProblemType;
  teeth: number[];
  notes: string | null;
  impactionType: ImpactionType | null;
  implantDetail: ImplantDetail | null;
  rootCanalLengths: RootCanalLength[];
}

export interface TreatmentCreateRequest {
  patientId: string;
  treatmentDate: string;
  treatmentType: TreatmentType;
  treatmentAmount: number;
  doctorNotes: string | null;
  problems: ClinicalProblem[];
  prescribedMedicines: PrescriptionItemRequest[];
}

export interface FollowUpCreateRequest {
  patientId: string;
  relatedTreatmentId: string | null;
  followUpDate: string;
  doctorNotes: string | null;
  newProblems: ClinicalProblem[];
  prescribedMedicines: PrescriptionItemRequest[];
}

export interface ScanResponse {
  id: number;
  originalFilename: string;
  contentType: string;
  fileSizeBytes: number;
  createdAt: string;
}

export interface TreatmentResponse {
  treatmentId: string;
  patientId: string;
  patientName: string;
  treatmentDate: string;
  treatmentType: TreatmentType;
  treatmentAmount: number;
  doctorNotes: string | null;
  problems: ClinicalProblem[];
  prescribedMedicines: PrescriptionItem[];
  scans: ScanResponse[];
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface FollowUpResponse {
  followUpId: string;
  patientId: string;
  patientName: string;
  relatedTreatmentId: string | null;
  followUpDate: string;
  doctorNotes: string | null;
  newProblems: ClinicalProblem[];
  prescribedMedicines: PrescriptionItem[];
  scans: ScanResponse[];
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface FollowUpContext {
  latestTreatment: TreatmentResponse | null;
  previousFollowUps: FollowUpResponse[];
}

export interface PatientHealthSnapshot {
  appointmentId: string;
  appointmentDateTime: string;
  systolicBp: number | null;
  diastolicBp: number | null;
  heartRate: number | null;
  breathingRate: number | null;
  heightCm: number | null;
  weightKg: number | null;
  bmi: number | null;
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

export const problemTypeLabels: Record<ProblemType, string> = {
  ROOT_CANAL: 'Root Canal',
  IMPLANTS: 'Implants',
  IMPACTION: 'Impaction',
  CROWNS_BRIDGES: 'Crowns/Bridges',
  BRACES: 'Braces',
  DENTURES: 'Dentures',
  EXTRACTION: 'Extraction',
  FILLING: 'Filling',
  SCALING: 'Scaling',
  CERVICAL_ABRASION: 'Cervical Abrasion',
  GENERALIZED_GINGIVITIS: 'Generalized Gingivitis'
};

export const impactionTypeLabels: Record<ImpactionType, string> = {
  MESIO_ANGULAR: 'Mesio-angular',
  DISTO_ANGULAR: 'Disto-angular',
  VERTICAL: 'Vertical',
  HORIZONTAL: 'Horizontal'
};

export const implantTypeLabels: Record<ImplantType, string> = {
  BASAL: 'Basal',
  CONVENTIONAL: 'Conventional'
};
