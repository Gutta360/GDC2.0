import { Routes } from '@angular/router';

import { Login } from './features/auth/login/login';

import { Home } from './features/home/home';

import { AppShell } from './layout/app-shell/app-shell';
import { PatientRegistration } from './features/patients/registration/patient-registration/patient-registration';
import { PatientDetails } from './features/patients/details/patient-details/patient-details';
import { PatientEdit } from './features/patients/edit/patient-edit/patient-edit';

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
  path: 'patients/register',
  component: PatientRegistration
},
{
  path: 'patients/details',
  component: PatientDetails
},
{
  path: 'patients/:patientId/edit',
  component: PatientEdit
}

    ]

  },

  {

    path: '**',

    redirectTo: 'login'

  }

];