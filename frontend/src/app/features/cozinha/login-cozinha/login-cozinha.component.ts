import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import {
  CredenciaisLogin,
  LoginFormComponent,
} from '../../../shared/login-form/login-form.component';

export const ROTA_APOS_LOGIN_COZINHA = '/cozinha/fila';
export const MENSAGEM_LOGIN_INVALIDO = 'Usuário ou senha inválidos.';
export const SUBTITULO_LOGIN_COZINHA = 'Entre para acompanhar a fila de pedidos';

@Component({
  selector: 'app-login-cozinha',
  imports: [LoginFormComponent],
  templateUrl: './login-cozinha.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginCozinhaComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly subtitulo = SUBTITULO_LOGIN_COZINHA;
  protected readonly erro = signal<string | null>(null);
  protected readonly enviando = signal(false);

  protected entrar(credenciais: CredenciaisLogin): void {
    this.erro.set(null);
    this.enviando.set(true);
    this.authService.loginFuncionario(credenciais).subscribe({
      next: () => this.router.navigateByUrl(ROTA_APOS_LOGIN_COZINHA),
      error: () => {
        this.enviando.set(false);
        this.erro.set(MENSAGEM_LOGIN_INVALIDO);
      },
    });
  }
}
