import { Component, OnInit, inject, signal } from '@angular/core';
import { CatalogService } from '../../core/services/catalog';
import { Cocktail } from '../../core/models/cocktail';
import { ETIQUETAS_DESTILADO } from '../../core/models/spirits';

@Component({
  imports: [],
  selector: 'app-catalog',
  styleUrl: './catalog.css',
  templateUrl: './catalog.html',
})
export class Catalog implements OnInit {
  private readonly catalogService = inject(CatalogService);

  protected readonly cocktails = signal<Cocktail[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal('');

  protected etiquetaDestilado(destilado: string | null): string {
    if (!destilado) {
      return '';
    }
    return ETIQUETAS_DESTILADO[destilado] ?? destilado;
  }

  ngOnInit(): void {
    this.catalogService.getCocktails().subscribe({
      next: (data) => {
        this.cocktails.set(data);
        this.cargando.set(false);
      },
      error: (err) => {
        console.error(err);
        this.error.set('No se pudo cargar la carta (¿esta el gateway corriendo?)');
        this.cargando.set(false);
      },
    });
  }
}
