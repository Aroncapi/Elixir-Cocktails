import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cocktail } from '../models/cocktail';

export interface CocktailPayload {
  name: string;
  category: string;
  ingredients: string;
  imageUrl: string | null;
  active: boolean;
  eventTypeIds: number[];
}

export interface EventTypeAdmin {
  id: number;
  code: string;
  name: string;
  active: boolean;
  sortOrder: number;
}

@Injectable({ providedIn: 'root' })
export class AdminCatalogService {
  private readonly http = inject(HttpClient);

  getCocktails(): Observable<Cocktail[]> {
    return this.http.get<Cocktail[]>('/api/cocktails');
  }

  getEventTypes(): Observable<EventTypeAdmin[]> {
    return this.http.get<EventTypeAdmin[]>('/api/event-types');
  }

  create(payload: CocktailPayload): Observable<Cocktail> {
    return this.http.post<Cocktail>('/api/cocktails', payload);
  }

  update(id: number, payload: CocktailPayload): Observable<Cocktail> {
    return this.http.put<Cocktail>(`/api/cocktails/${id}`, payload);
  }

  deactivate(id: number): Observable<Cocktail> {
    return this.http.delete<Cocktail>(`/api/cocktails/${id}`);
  }
}
