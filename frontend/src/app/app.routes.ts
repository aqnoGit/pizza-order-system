import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'garcom',
    loadChildren: () => import('./features/garcom/garcom.routes').then((m) => m.garcomRoutes),
  },
];
