import { Routes } from '@angular/router';

import { Login } from './features/auth/login/login';

import { Home } from './features/home/home';

import { Register } from './features/patients/register/register';

import { AppShell } from './layout/app-shell/app-shell';


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

        path: 'patients/registration',

        component: Register

      }

    ]

  },

  {

    path: '**',

    redirectTo: 'login'

  }

];