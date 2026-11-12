import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { Perfil, ROTA_LOGIN, perfilTemAcesso } from './perfil';

export function perfilGuard(...permitidos: Perfil[]): CanActivateFn {
  return () => {
    const temAcesso = perfilTemAcesso(inject(AuthService).perfilAtual(), permitidos);
    return temAcesso || inject(Router).createUrlTree([ROTA_LOGIN]);
  };
}
