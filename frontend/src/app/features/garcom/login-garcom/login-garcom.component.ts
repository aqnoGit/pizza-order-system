import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import {
  CredenciaisLogin,
  LoginFormComponent,
} from '../../../shared/login-form/login-form.component';

export const ROTA_APOS_LOGIN_GARCOM = '/garcom/mesas';
export const MENSAGEM_LOGIN_INVALIDO = 'Usuário ou senha inválidos.';
export const SUBTITULO_LOGIN_GARCOM = 'Entre para gerenciar as mesas do salão';

@Component({
  selector: 'app-login-garcom',
  imports: [LoginFormComponent],
  templateUrl: './login-garcom.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginGarcomComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly subtitulo = SUBTITULO_LOGIN_GARCOM;
  protected readonly erro = signal<string | null>(null);
  protected readonly enviando = signal(false);

  protected entrar(credenciais: CredenciaisLogin): void {
    this.erro.set(null);
    this.enviando.set(true);
    this.authService.loginFuncionario(credenciais).subscribe({
      next: () => this.router.navigateByUrl(ROTA_APOS_LOGIN_GARCOM),
      error: () => {
        this.enviando.set(false);
        this.erro.set(MENSAGEM_LOGIN_INVALIDO);
      },
    });
  }
}
