import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PatientForm } from '../../components/patient-form/patient-form';


@Component({
  selector: 'app-patient-edit',
  standalone: true,
  imports: [
    CommonModule,
    PatientForm
  ],
  templateUrl: './patient-edit.html',
  styleUrl: './patient-edit.scss'
})
export class PatientEdit implements OnInit {

  patientId = '';

  loading = false;
  loadingPatient = true;

  patient: any = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}


  ngOnInit(): void {

    this.patientId =
      this.route.snapshot.paramMap.get(
        'patientId'
      ) ?? '';

    this.loadPatient();
  }


  private loadPatient(): void {

    /*
     * NEXT:
     *
     * GET /api/v1/patients/{patientId}
     */

    this.patient = {

      patientId:
        this.patientId,

      registrationDate:
        '2026-09-26T11:50:00',

      firstName:
        'Rames',

      lastName:
        'Doctor',

      gender:
        'MALE',

      age:
        40,

      mobile:
        '9999988887',

      address:
        'Nallagandla, Hyderabad',

      referredBy:
        'DOCTOR',

      doctorName:
        'Dr Rao',

      consultationFee:
        750

    };

    this.loadingPatient = false;
  }


  onPatientUpdate(
    patientData: any
  ): void {

    this.loading = true;

    console.log(
      'Update patient:',
      this.patientId,
      patientData
    );

    /*
     * NEXT:
     *
     * PUT
     * /api/v1/patients/{patientId}
     */

    this.loading = false;
  }


  close(): void {

    this.router.navigate(
      ['/patients/details'],
      {
        queryParams: {
          patientId: this.patientId
        }
      }
    );
  }

}