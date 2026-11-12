import { Routes } from '@angular/router';

export const cozinhaRoutes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./login-cozinha/login-cozinha.component').then((m) => m.LoginCozinhaComponent),
  },
];
