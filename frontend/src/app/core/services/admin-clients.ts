import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cliente } from '../models/client';

export interface ClientePayload {
  name: string;
  email: string | null;
  whatsapp: string | null;
  location: string | null;
  tier: string;
  totalSpent: number | null;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class AdminClientsService {
  private readonly http = inject(HttpClient);

  list(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>('/api/clients');
  }

  search(busqueda: string, tier: string): Observable<Cliente[]> {
    let params = new HttpParams();
    if (busqueda) {
      params = params.set('search', busqueda);
    }
    if (tier) {
      params = params.set('tier', tier);
    }
    return this.http.get<Cliente[]>('/api/clients', { params });
  }

  update(id: number, payload: ClientePayload): Observable<Cliente> {
    return this.http.put<Cliente>(`/api/clients/${id}`, payload);
  }

  deactivate(id: number): Observable<Cliente> {
    return this.http.delete<Cliente>(`/api/clients/${id}`);
  }
}
