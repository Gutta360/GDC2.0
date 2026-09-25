import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class Register implements OnInit {

  registerForm!: FormGroup;

  loading = false;

  readonly genderOptions = [
    { value: 'MALE', label: 'Male' },
    { value: 'FEMALE', label: 'Female' },
    { value: 'OTHER', label: 'Other' }
  ];

  readonly referredByOptions = [
    { value: 'DOCTOR', label: 'Doctor' },
    { value: 'PATIENT', label: 'Patient' },
    { value: 'ONLINE', label: 'Online' },
    { value: 'SELF', label: 'Self' },
    { value: 'OTHER', label: 'Other' }
  ];

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {

    this.registerForm = this.fb.group({

      registrationDate: [
        this.toDateTimeLocalValue(new Date()),
        Validators.required
      ],

      firstName: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(50),
          Validators.pattern(/^[A-Za-z ]+$/)
        ]
      ],

      lastName: [
        '',
        [
          Validators.maxLength(50),
          Validators.pattern(/^[A-Za-z ]*$/)
        ]
      ],

      gender: [
        '',
        Validators.required
      ],

      age: [
        '',
        [
          Validators.required,
          Validators.min(1),
          Validators.max(120)
        ]
      ],

      mobile: [
        '',
        [
          Validators.required,
          Validators.pattern(/^[0-9]{10}$/)
        ]
      ],

      address: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(100)
        ]
      ],

      referredBy: [
        '',
        Validators.required
      ],

      doctorName: [
        ''
      ],

      consultationFee: [
        '0.00',
        [
          Validators.required,
          this.consultationFeeValidator
        ]
      ]

    });

    this.registerForm
      .get('referredBy')
      ?.valueChanges
      .subscribe(value => {
        this.updateDoctorNameValidation(value);
      });
  }


  /* =========================
     Convenient Form Access
     ========================= */

  get f() {
    return this.registerForm.controls;
  }


  get isDoctorReferral(): boolean {
    return this.registerForm.get('referredBy')?.value === 'DOCTOR';
  }


  /* =========================
     Doctor Validation
     ========================= */

  private updateDoctorNameValidation(referredBy: string): void {

    const doctorName = this.registerForm.get('doctorName');

    if (!doctorName) {
      return;
    }

    if (referredBy === 'DOCTOR') {

      doctorName.setValidators([
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(50),
        Validators.pattern(/^[A-Za-z ]+$/)
      ]);

    } else {

      doctorName.clearValidators();
      doctorName.setValue('', { emitEvent: false });

    }

    doctorName.updateValueAndValidity();
  }


  /* =========================
     Consultation Fee Validation
     ========================= */

  private consultationFeeValidator(
    control: AbstractControl
  ): ValidationErrors | null {

    const value = String(control.value ?? '').trim();

    if (!value) {
      return null;
    }

    if (!/^\d+(\.\d{1,2})?$/.test(value)) {
      return {
        invalidFee: true
      };
    }

    const amount = Number(value);

    if (amount < 0) {
      return {
        negativeFee: true
      };
    }

    return null;
  }


  /* =========================
     Registration Date
     ========================= */

  private toDateTimeLocalValue(date: Date): string {

    const pad = (value: number): string =>
      value.toString().padStart(2, '0');

    return (
      `${date.getFullYear()}-` +
      `${pad(date.getMonth() + 1)}-` +
      `${pad(date.getDate())}T` +
      `${pad(date.getHours())}:` +
      `${pad(date.getMinutes())}`
    );
  }


  /* =========================
     Input Cleanup
     ========================= */

  normalizeName(controlName: string): void {

    const control = this.registerForm.get(controlName);

    if (!control) {
      return;
    }

    let value = String(control.value ?? '');

    value = value
      .replace(/[^A-Za-z ]/g, '')
      .replace(/\s+/g, ' ')
      .replace(/^\s+/, '');

    control.setValue(value, {
      emitEvent: false
    });
  }


  normalizeMobile(): void {

    const control = this.registerForm.get('mobile');

    if (!control) {
      return;
    }

    const value = String(control.value ?? '')
      .replace(/\D/g, '')
      .slice(0, 10);

    control.setValue(value, {
      emitEvent: false
    });
  }


  /* =========================
     Create Patient
     ========================= */

  onCreate(): void {

    if (this.registerForm.invalid) {

      this.registerForm.markAllAsTouched();
      return;

    }

    const formValue = this.registerForm.getRawValue();

    const request = {

      firstName:
        formValue.firstName.trim(),

      lastName:
        formValue.lastName?.trim() || null,

      gender:
        formValue.gender,

      age:
        Number(formValue.age),

      mobile:
        formValue.mobile,

      address:
        formValue.address.trim(),

      referredBy:
        formValue.referredBy,

      doctorName:
        formValue.referredBy === 'DOCTOR'
          ? formValue.doctorName.trim()
          : null,

      consultationFee:
        Number(formValue.consultationFee),

      registrationDate:
        new Date(formValue.registrationDate).toISOString()

    };

    console.log('Patient Request:', request);

    /*
     * API integration comes next.
     *
     * POST /api/v1/patients
     */

  }

}