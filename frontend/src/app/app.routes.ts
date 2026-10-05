import { Routes } from '@angular/router';

import { Login } from './features/auth/login/login';

import { Home } from './features/home/home';

import { AppShell } from './layout/app-shell/app-shell';
import { PatientRegistration } from './features/patients/registration/patient-registration/patient-registration';
import { PatientDetails } from './features/patients/details/patient-details/patient-details';
import { PatientEdit } from './features/patients/edit/patient-edit/patient-edit';
import { PatientSummary } from './features/patients/summary/patient-summary/patient-summary';
import { AppointmentCalendar } from './features/appointments/calendar/appointment-calendar';
import { Treatment } from './features/treatments/treatment/treatment';
import { FollowUp } from './features/treatments/follow-up/follow-up';
import { PharmacyPayment } from './features/pharmacy/payment/payment';
import { MedicineStock } from './features/pharmacy/stock/stock';
import { Payment } from './features/payments/payment/payment';
import { PaymentHistory } from './features/payments/history/payment-history';

export const routes: Routes = [

  {

    path: '',

    redirectTo: 'login',

    pathMatch: 'full'

  },

  {

    path: 'login',

    component: Login

  },

  {

    path: '',

    component: AppShell,

    children: [

      {

        path: 'home',

        component: Home

      },

      {

        path: 'patients',

        redirectTo: 'patients/registration',

        pathMatch: 'full'

      },

      {

        path: 'patients/registration',

        component: PatientRegistration

      },

      {

        path: 'patients/details',

        component: PatientDetails

      },

      {

        path: 'patients/summary',

        component: PatientSummary

      },

      {

        path: 'patients/:patientId/edit',

        component: PatientEdit

      },

      {

        path: 'appointments',

        redirectTo: 'appointments/calendar',

        pathMatch: 'full'

      },

      {

        path: 'appointments/calendar',

        component: AppointmentCalendar

      },

      {

        path: 'treatments',

        component: Treatment

      },

      {

        path: 'treatments/follow-up',

        component: FollowUp

      },

      {

        path: 'payments',

        component: Payment

      },

      {

        path: 'payments/history',

        component: PaymentHistory

      },

      {

        path: 'pharmacy',

        component: PharmacyPayment

      },

      {

        path: 'pharmacy/stock',

        component: MedicineStock

      }

    ]

  },

  {

    path: '**',

    redirectTo: 'login'

  }

];
