import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WHATSAPP_OWNER, setDuenoWhatsapp } from '../utils/whatsapp';
import { setTipoCambio } from '../utils/dinero';

export interface Settings {
  whatsappOwner: string;
  contactEmail: string;
  businessName: string;
  exchangeRate: number;
  baseHours: number;
  extraHourPen: number;
}

/** El panel solo edita estos campos; baseHours/extraHourPen son de solo lectura. */
export type SettingsEditable = Omit<Settings, 'baseHours' | 'extraHourPen'>;

@Injectable({ providedIn: 'root' })
export class SettingsService {
  private readonly http = inject(HttpClient);

  readonly whatsappDueno = signal(WHATSAPP_OWNER);
  readonly contactoEmail = signal('');
  readonly nombreNegocio = signal('');
  readonly tipoCambio = signal(3.4);
  readonly horasBase = signal(5);
  readonly horaExtra = signal(40);
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

  guardar(settings: SettingsEditable): Observable<Settings> {
    return this.http.put<Settings>('/api/settings', settings);
  }

  private aplicar(settings: Settings): void {
    setDuenoWhatsapp(settings.whatsappOwner);
    setTipoCambio(settings.exchangeRate);
    this.whatsappDueno.set(settings.whatsappOwner);
    this.contactoEmail.set(settings.contactEmail);
    this.nombreNegocio.set(settings.businessName);
    this.tipoCambio.set(settings.exchangeRate);
    this.horasBase.set(settings.baseHours);
    this.horaExtra.set(settings.extraHourPen);
    this.cargado.set(true);
  }
}
