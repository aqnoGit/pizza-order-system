import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ROTA_LOGIN, rotaInicialDoPerfil } from '../../core/auth/perfil';
import { CredenciaisLogin, LoginFormComponent } from '../../shared/login-form/login-form.component';

export const MENSAGEM_LOGIN_INVALIDO = 'Usuário ou senha inválidos.';
export const SUBTITULO_LOGIN = 'Entre com usuário e senha para acessar o sistema';

@Component({
  selector: 'app-login',
  imports: [LoginFormComponent],
  templateUrl: './login.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly subtitulo = SUBTITULO_LOGIN;
  protected readonly erro = signal<string | null>(null);
  protected readonly enviando = signal(false);

  protected entrar(credenciais: CredenciaisLogin): void {
    this.erro.set(null);
    this.enviando.set(true);
    this.authService.loginFuncionario(credenciais).subscribe({
      next: () => this.redirecionarPeloPerfil(),
      error: () => {
        this.enviando.set(false);
        this.erro.set(MENSAGEM_LOGIN_INVALIDO);
      },
    });
  }

  private redirecionarPeloPerfil(): void {
    const perfil = this.authService.perfilAtual();
    this.router.navigateByUrl(perfil ? rotaInicialDoPerfil(perfil) : ROTA_LOGIN);
  }
}
