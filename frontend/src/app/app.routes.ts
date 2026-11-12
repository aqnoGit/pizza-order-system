import { Routes } from '@angular/router';
import { perfilGuard } from './core/auth/perfil.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then((m) => m.LoginComponent),
  },
  { path: 'garcom/login', redirectTo: 'login' },
  { path: 'cozinha/login', redirectTo: 'login' },
  {
    path: 'garcom',
    canActivate: [perfilGuard('GARCOM')],
    loadChildren: () => import('./features/garcom/garcom.routes').then((m) => m.garcomRoutes),
  },
  {
    path: 'cozinha',
    canActivate: [perfilGuard('COZINHA')],
    loadChildren: () => import('./features/cozinha/cozinha.routes').then((m) => m.cozinhaRoutes),
  },
];
