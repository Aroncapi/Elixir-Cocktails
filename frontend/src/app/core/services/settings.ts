import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WHATSAPP_OWNER, setDuenoWhatsapp } from '../utils/whatsapp';

export interface Settings {
  whatsappOwner: string;
  contactEmail: string;
  businessName: string;
}

@Injectable({ providedIn: 'root' })
export class SettingsService {
  private readonly http = inject(HttpClient);

  readonly whatsappDueno = signal(WHATSAPP_OWNER);
  readonly contactoEmail = signal('');
  readonly nombreNegocio = signal('');
  readonly cargado = signal(false);

  cargar(): void {
    this.consultar().subscribe({
      next: (settings) => this.aplicar(settings),
      error: () => this.cargado.set(true),
    });
  }

  consultar(): Observable<Settings> {
    return this.http.get<Settings>('/api/public/settings');
  }

  guardar(settings: Settings): Observable<Settings> {
    return this.http.put<Settings>('/api/settings', settings);
  }

  private aplicar(settings: Settings): void {
    setDuenoWhatsapp(settings.whatsappOwner);
    this.whatsappDueno.set(settings.whatsappOwner);
    this.contactoEmail.set(settings.contactEmail);
    this.nombreNegocio.set(settings.businessName);
    this.cargado.set(true);
  }
}
