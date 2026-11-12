import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { CHAVE_TOKEN } from './auth.service';
import { Perfil } from './perfil';
import { perfilGuard } from './perfil.guard';

function tokenDoPerfil(perfil: string): string {
  return `cabecalho.${btoa(JSON.stringify({ perfil }))}.assinatura`;
}

describe('perfilGuard', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideHttpClient()] });
  });

  function executar(...permitidos: Perfil[]) {
    return TestBed.runInInjectionContext(() =>
      perfilGuard(...permitidos)({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );
  }

  it('libera o perfil permitido', () => {
    localStorage.setItem(CHAVE_TOKEN, tokenDoPerfil('GARCOM'));
    expect(executar('GARCOM')).toBe(true);
  });

  it('libera o admin em qualquer area', () => {
    localStorage.setItem(CHAVE_TOKEN, tokenDoPerfil('ADMIN'));
    expect(executar('COZINHA')).toBe(true);
  });

  it('manda para o login quando o perfil nao e permitido', () => {
    localStorage.setItem(CHAVE_TOKEN, tokenDoPerfil('GARCOM'));
    const resultado = executar('COZINHA') as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(resultado)).toBe('/login');
  });

  it('manda para o login sem token', () => {
    const resultado = executar('GARCOM') as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(resultado)).toBe('/login');
  });
});
