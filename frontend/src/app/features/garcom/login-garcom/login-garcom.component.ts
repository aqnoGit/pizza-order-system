import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

export const ROTA_APOS_LOGIN_GARCOM = '/garcom/mesas';
export const MENSAGEM_LOGIN_INVALIDO = 'Usuário ou senha inválidos.';

@Component({
  selector: 'app-login-garcom',
  imports: [ReactiveFormsModule],
  templateUrl: './login-garcom.component.html',
  styleUrl: './login-garcom.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginGarcomComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly formBuilder = inject(NonNullableFormBuilder);

  protected readonly formulario = this.formBuilder.group({
    usuario: ['', Validators.required],
    senha: ['', Validators.required],
  });
  protected readonly erro = signal<string | null>(null);
  protected readonly enviando = signal(false);

  protected entrar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.erro.set(null);
    this.enviando.set(true);
    this.authService.loginFuncionario(this.formulario.getRawValue()).subscribe({
      next: () => this.router.navigateByUrl(ROTA_APOS_LOGIN_GARCOM),
      error: () => {
        this.enviando.set(false);
        this.erro.set(MENSAGEM_LOGIN_INVALIDO);
      },
    });
  }
}
