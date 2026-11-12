import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { CHAVE_TOKEN } from '../../core/auth/auth.service';
import { LoginComponent, MENSAGEM_LOGIN_INVALIDO } from './login.component';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let http: HttpTestingController;
  let router: Router;
  let elemento: HTMLElement;

  function preencher(usuario: string, senha: string): void {
    const usuarioInput = elemento.querySelector<HTMLInputElement>('#usuario')!;
    const senhaInput = elemento.querySelector<HTMLInputElement>('#senha')!;
    usuarioInput.value = usuario;
    usuarioInput.dispatchEvent(new Event('input'));
    senhaInput.value = senha;
    senhaInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();
  }

  function enviar(): void {
    elemento.querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    fixture = TestBed.createComponent(LoginComponent);
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    elemento = fixture.nativeElement;
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('bloqueia o envio e nao chama a API com campos vazios', () => {
    const botao = elemento.querySelector<HTMLButtonElement>('button[type="submit"]')!;
    expect(botao.disabled).toBe(true);

    enviar();

    http.expectNone(`${environment.apiUrl}/auth/funcionario`);
  });

  function tokenDoPerfil(perfil: string): string {
    return `a.${btoa(JSON.stringify({ perfil }))}.c`;
  }

  function logarComo(perfil: string): void {
    preencher('joao', '123');
    enviar();
    http.expectOne(`${environment.apiUrl}/auth/funcionario`).flush({ token: tokenDoPerfil(perfil) });
  }

  it('salva o token e envia a API as credenciais', () => {
    preencher('joao', '123');
    enviar();

    const req = http.expectOne(`${environment.apiUrl}/auth/funcionario`);
    expect(req.request.body).toEqual({ usuario: 'joao', senha: '123' });
    req.flush({ token: tokenDoPerfil('GARCOM') });

    expect(localStorage.getItem(CHAVE_TOKEN)).toBe(tokenDoPerfil('GARCOM'));
  });

  it('redireciona o garcom para a area do garcom', () => {
    logarComo('GARCOM');
    expect(router.navigateByUrl).toHaveBeenCalledWith('/garcom');
  });

  it('redireciona a cozinha para a area da cozinha', () => {
    logarComo('COZINHA');
    expect(router.navigateByUrl).toHaveBeenCalledWith('/cozinha');
  });

  it('redireciona o admin para uma area com acesso liberado', () => {
    logarComo('ADMIN');
    expect(router.navigateByUrl).toHaveBeenCalledWith('/garcom');
  });

  it('mostra o subtitulo generico e a caixa mostrar senha', () => {
    expect(elemento.querySelector('.subtitulo')?.textContent).toContain('acessar o sistema');
    expect(elemento.querySelector('.mostrar-senha')?.textContent).toContain('Mostrar senha');
  });

  it('mostra erro e nao salva nada quando as credenciais sao invalidas', () => {
    preencher('joao', 'errada');
    enviar();

    http
      .expectOne(`${environment.apiUrl}/auth/funcionario`)
      .flush({}, { status: 401, statusText: 'Unauthorized' });
    fixture.detectChanges();

    expect(elemento.querySelector('.erro')?.textContent).toContain(MENSAGEM_LOGIN_INVALIDO);
    expect(localStorage.getItem(CHAVE_TOKEN)).toBeNull();
    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });
});
