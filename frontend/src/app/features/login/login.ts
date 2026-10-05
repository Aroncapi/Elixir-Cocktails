import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth';

@Component({
  imports: [FormsModule, RouterLink],
  selector: 'app-login',
  styleUrl: './login.css',
  templateUrl: './login.html',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected email = '';
  protected password = '';

  protected readonly cargando = signal(false);
  protected readonly error = signal('');

  protected iniciarSesion(): void {
    if (this.cargando() || !this.email || !this.password) {
      return;
    }

    this.error.set('');
    this.cargando.set(true);

    this.auth.login(this.email, this.password).subscribe({
      next: () => {
        this.cargando.set(false);
        this.router.navigateByUrl('/');
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(
          err?.status === 401
            ? 'Correo o contraseña incorrectos.'
            : 'No se pudo conectar con el servidor (¿está el gateway corriendo?).'
        );
      },
    });
  }
}
