import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth';
import { SettingsService } from '../../core/services/settings';

@Component({
  imports: [FormsModule, RouterLink, RouterLinkActive],
  selector: 'app-configuracion',
  styleUrls: ['./requests-admin.css', './configuracion.css'],
  templateUrl: './configuracion.html',
})
export class Configuracion implements OnInit {
  private readonly settings = inject(SettingsService);
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);

  protected readonly menuAbierto = signal(false);
  protected readonly guardando = signal(false);
  protected readonly guardado = signal(false);
  protected readonly error = signal('');

  protected whatsapp = '';
  protected contactEmail = '';
  protected businessName = '';

  ngOnInit(): void {
    this.settings.consultar().subscribe({
      next: (settings) => {
        this.whatsapp = settings.whatsappOwner;
        this.contactEmail = settings.contactEmail;
        this.businessName = settings.businessName;
      },
      error: () => this.error.set('No se pudo cargar la configuracion.'),
    });
  }

  protected alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  protected cerrarSesion(): void {
    this.auth.logout();
    this.router.navigateByUrl('/');
  }

  protected guardar(): void {
    if (!this.whatsapp.trim()) {
      this.error.set('El WhatsApp del negocio es obligatorio.');
      return;
    }

    this.error.set('');
    this.guardado.set(false);
    this.guardando.set(true);

    this.settings
      .guardar({
        whatsappOwner: this.whatsapp.trim(),
        contactEmail: this.contactEmail.trim(),
        businessName: this.businessName.trim(),
      })
      .subscribe({
        next: (settings) => {
          this.whatsapp = settings.whatsappOwner;
          this.contactEmail = settings.contactEmail;
          this.businessName = settings.businessName;
          this.guardando.set(false);
          this.guardado.set(true);
          setTimeout(() => this.guardado.set(false), 3000);
        },
        error: (err) => {
          this.guardando.set(false);
          this.error.set(err?.error?.message ?? 'No se pudo guardar la configuracion.');
        },
      });
  }

  protected vistaPrevia(): string {
    const limpio = this.whatsapp.replace(/[^\d]/g, '');
    return limpio ? `https://wa.me/${limpio}` : 'https://wa.me/';
  }
}
