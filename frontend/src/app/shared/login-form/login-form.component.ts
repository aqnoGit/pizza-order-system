import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideArrowRight, lucideCircleHelp, lucideEye, lucideEyeOff, lucideLock, lucideUser } from '@ng-icons/lucide';
import { saudacaoPorHora } from './saudacao';

export interface CredenciaisLogin {
  usuario: string;
  senha: string;
}

export const VERSAO_APLICACAO = '1.0v';

@Component({
  selector: 'app-login-form',
  imports: [ReactiveFormsModule, NgIcon],
  viewProviders: [provideIcons({ lucideUser, lucideLock, lucideEye, lucideEyeOff, lucideCircleHelp, lucideArrowRight })],
  templateUrl: './login-form.component.html',
  styleUrl: './login-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginFormComponent {
  private readonly formBuilder = inject(NonNullableFormBuilder);

  readonly subtitulo = input.required<string>();
  readonly erro = input<string | null>(null);
  readonly enviando = input(false);
  readonly enviar = output<CredenciaisLogin>();

  protected readonly saudacao = saudacaoPorHora(new Date().getHours());
  protected readonly versao = VERSAO_APLICACAO;
  protected readonly senhaVisivel = signal(false);
  protected readonly formulario = this.formBuilder.group({
    usuario: ['', Validators.required],
    senha: ['', Validators.required],
  });

  protected alternarSenhaVisivel(): void {
    this.senhaVisivel.update((visivel) => !visivel);
  }

  protected entrar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.enviar.emit(this.formulario.getRawValue());
  }
}
