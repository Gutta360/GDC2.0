import { Component } from '@angular/core';
import { PatientForm } from '../../components/patient-form/patient-form';

@Component({
  selector: 'app-patient-registration',
  standalone: true,
  imports: [
    PatientForm
  ],
  templateUrl: './patient-registration.html',
  styleUrl: './patient-registration.scss'
})
export class PatientRegistration {

  loading = false;

  onPatientCreate(patientData: any): void {

    console.log(
      'Patient Registration Data:',
      patientData
    );

    /*
     * Next step:
     *
     * POST /api/v1/patients
     */
  }

}