import { Routes } from '@angular/router';

export const garcomRoutes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./login-garcom/login-garcom.component').then((m) => m.LoginGarcomComponent),
  },
];
