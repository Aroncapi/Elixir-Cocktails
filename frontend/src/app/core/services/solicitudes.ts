import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ServiceTier, Solicitud, SolicitudPayload } from '../models/solicitud';

@Injectable({ providedIn: 'root' })
export class SolicitudesService {
  private readonly http = inject(HttpClient);

  crear(payload: SolicitudPayload): Observable<Solicitud> {
    return this.http.post<Solicitud>('/api/public/solicitudes', payload);
  }

  porToken(token: string): Observable<Solicitud> {
    return this.http.get<Solicitud>(`/api/public/solicitudes/${token}`);
  }

  getTiers(): Observable<ServiceTier[]> {
    return this.http.get<ServiceTier[]>('/api/public/service-tiers');
  }
}
