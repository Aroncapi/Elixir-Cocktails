import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Usuario } from '../models/usuario';

interface RespuestaLogin {
  token: string;
  type: string;
  user: Usuario;
}

const CLAVE_TOKEN = 'velvet_token';
const CLAVE_USUARIO = 'velvet_usuario';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  readonly token = signal<string | null>(localStorage.getItem(CLAVE_TOKEN));
  readonly usuario = signal<Usuario | null>(this.leerUsuario());

  get autenticado(): boolean {
    return this.token() !== null;
  }

  get esAdmin(): boolean {
    return this.usuario()?.role === 'ADMIN';
  }

  login(email: string, password: string): Observable<RespuestaLogin> {
    return this.http.post<RespuestaLogin>('/api/auth/login', { email, password }).pipe(
      tap((respuesta) => {
        localStorage.setItem(CLAVE_TOKEN, respuesta.token);
        localStorage.setItem(CLAVE_USUARIO, JSON.stringify(respuesta.user));
        this.token.set(respuesta.token);
        this.usuario.set(respuesta.user);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(CLAVE_TOKEN);
    localStorage.removeItem(CLAVE_USUARIO);
    this.token.set(null);
    this.usuario.set(null);
  }

  private leerUsuario(): Usuario | null {
    const dato = localStorage.getItem(CLAVE_USUARIO);
    if (!dato) {
      return null;
    }
    try {
      return JSON.parse(dato) as Usuario;
    } catch {
      return null;
    }
  }
}
