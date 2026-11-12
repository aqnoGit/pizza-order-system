import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'garcom',
    loadChildren: () => import('./features/garcom/garcom.routes').then((m) => m.garcomRoutes),
  },
  {
    path: 'cozinha',
    loadChildren: () => import('./features/cozinha/cozinha.routes').then((m) => m.cozinhaRoutes),
  },
];
