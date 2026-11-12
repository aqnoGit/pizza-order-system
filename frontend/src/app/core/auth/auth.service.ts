import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Perfil } from './perfil';

export interface LoginFuncionarioRequest {
  usuario: string;
  senha: string;
}

export interface TokenResponse {
  token: string;
}

export const CHAVE_TOKEN = 'token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  loginFuncionario(requisicao: LoginFuncionarioRequest): Observable<TokenResponse> {
    return this.http
      .post<TokenResponse>(`${environment.apiUrl}/auth/funcionario`, requisicao)
      .pipe(tap((resposta) => localStorage.setItem(CHAVE_TOKEN, resposta.token)));
  }

  perfilAtual(): Perfil | null {
    const token = localStorage.getItem(CHAVE_TOKEN);
    if (!token) {
      return null;
    }
    try {
      const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const { perfil } = JSON.parse(atob(payload)) as { perfil?: Perfil };
      return perfil ?? null;
    } catch {
      return null;
    }
  }
}
