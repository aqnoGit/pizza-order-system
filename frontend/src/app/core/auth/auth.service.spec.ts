import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { AuthService, CHAVE_TOKEN } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envia as credenciais e guarda o token no localStorage', () => {
    service.loginFuncionario({ usuario: 'joao', senha: '123' }).subscribe();

    const req = http.expectOne(`${environment.apiUrl}/auth/funcionario`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ usuario: 'joao', senha: '123' });
    req.flush({ token: 'jwt-abc' });

    expect(localStorage.getItem(CHAVE_TOKEN)).toBe('jwt-abc');
  });

  it('le o perfil do token guardado', () => {
    localStorage.setItem(CHAVE_TOKEN, `a.${btoa(JSON.stringify({ perfil: 'ADMIN' }))}.c`);

    expect(service.perfilAtual()).toBe('ADMIN');
  });

  it('retorna nulo sem token ou com token invalido', () => {
    expect(service.perfilAtual()).toBeNull();

    localStorage.setItem(CHAVE_TOKEN, 'lixo');

    expect(service.perfilAtual()).toBeNull();
  });

  it('nao guarda token quando o backend responde erro', () => {
    service.loginFuncionario({ usuario: 'joao', senha: 'x' }).subscribe({ error: () => undefined });

    http
      .expectOne(`${environment.apiUrl}/auth/funcionario`)
      .flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(localStorage.getItem(CHAVE_TOKEN)).toBeNull();
  });
});
