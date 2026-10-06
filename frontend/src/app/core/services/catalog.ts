import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cocktail } from '../models/cocktail';

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private readonly http = inject(HttpClient);

  getCocktails(): Observable<Cocktail[]> {
    return this.http.get<Cocktail[]>('/api/public/cocktails');
  }

  getEventTypes() {
    return this.http.get<{
      id: number;
      code: string;
      name: string;
      description: string;
      active: boolean;
    }[]>('/api/public/event-types');
  }
}
