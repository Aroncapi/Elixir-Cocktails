import { Component, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from './core/services/auth';
import { SettingsService } from './core/services/settings';

@Component({
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly settings = inject(SettingsService);

  protected readonly menuAbierto = signal(false);

  /** El panel admin trae su propio layout, ahi se oculta la barra publica */
  protected readonly enPanel = signal(false);

  constructor() {
    this.settings.cargar();
    this.enPanel.set(this.router.url.startsWith('/panel'));
    this.router.events
      .pipe(filter((evento): evento is NavigationEnd => evento instanceof NavigationEnd))
      .subscribe((evento) => {
        this.enPanel.set(evento.urlAfterRedirects.startsWith('/panel'));
      });
  }

  protected alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  protected cerrarSesion(): void {
    this.auth.logout();
    this.menuAbierto.set(false);
    this.router.navigateByUrl('/');
  }
}
