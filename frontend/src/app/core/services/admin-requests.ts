import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  EventoCalendario,
  ResumenSolicitudes,
  Solicitud,
  SolicitudPage,
} from '../models/solicitud';

export interface PageParams {
  page: number;
  size: number;
  status?: string;
  search?: string;
}

@Injectable({ providedIn: 'root' })
export class AdminRequestsService {
  private readonly http = inject(HttpClient);

  page(params: PageParams): Observable<SolicitudPage> {
    let opciones = new HttpParams()
      .set('page', String(params.page))
      .set('size', String(params.size));
    if (params.status) {
      opciones = opciones.set('status', params.status);
    }
    if (params.search && params.search.trim() !== '') {
      opciones = opciones.set('search', params.search.trim());
    }
    return this.http.get<SolicitudPage>('/api/requests', { params: opciones });
  }

  resumen(): Observable<ResumenSolicitudes> {
    return this.http.get<ResumenSolicitudes>('/api/requests/resumen');
  }

  eventos(desde: string, hasta: string): Observable<EventoCalendario[]> {
    const opciones = new HttpParams().set('desde', desde).set('hasta', hasta);
    return this.http.get<EventoCalendario[]>('/api/requests/eventos', { params: opciones });
  }

  get(id: number): Observable<Solicitud> {
    return this.http.get<Solicitud>(`/api/requests/${id}`);
  }

  updateStatus(id: number, status: string): Observable<Solicitud> {
    return this.http.put<Solicitud>(`/api/requests/${id}/status`, { status });
  }
}
