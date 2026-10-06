import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { Cocktail } from '../../core/models/cocktail';
import { AdminCatalogService, CocktailPayload, EventTypeAdmin } from '../../core/services/admin-catalog';
import { AuthService } from '../../core/services/auth';

const ETIQUETAS_CATEGORIA: Record<string, string> = {
  SIGNATURE: 'Signature',
  CLASICOS: 'Clásicos',
  CITRICOS: 'Cítricos',
  SIN_ALCOHOL: 'Sin Alcohol',
};

@Component({
  imports: [FormsModule, RouterLink, RouterLinkActive],
  selector: 'app-catalog-admin',
  styleUrl: './catalog-admin.css',
  templateUrl: './catalog-admin.html',
})
export class CatalogAdmin implements OnInit {
  private readonly servicio = inject(AdminCatalogService);
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly cocktails = signal<Cocktail[]>([]);
  protected readonly eventTypes = signal<EventTypeAdmin[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal('');

  protected readonly busqueda = signal('');
  protected readonly categoriaFiltro = signal('');
  protected readonly filtrosVisibles = signal(false);

  protected readonly menuAbierto = signal(false);
  protected readonly modalAbierto = signal(false);
  protected readonly guardando = signal(false);
  protected readonly errorModal = signal('');
  protected readonly editandoId = signal<number | null>(null);

  protected readonly etiquetas = ETIQUETAS_CATEGORIA;

  protected formulario: CocktailPayload = this.formularioVacio();
  protected activeOriginal = true;

  protected readonly visibles = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    const categoria = this.categoriaFiltro();
    return this.cocktails().filter((c) => {
      const coincideTexto =
        texto === '' ||
        c.name.toLowerCase().includes(texto) ||
        c.ingredients.toLowerCase().includes(texto);
      const coincideCategoria = categoria === '' || c.category === categoria;
      return coincideTexto && coincideCategoria;
    });
  });

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    this.servicio.getCocktails().subscribe({
      next: (lista) => {
        this.cocktails.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el catálogo (¿está el gateway corriendo?)');
        this.cargando.set(false);
      },
    });

    this.servicio.getEventTypes().subscribe({
      next: (tipos) => this.eventTypes.set(tipos.filter((t) => t.active)),
      error: () => this.eventTypes.set([]),
    });
  }

  protected etiquetaCategoria(categoria: string): string {
    return ETIQUETAS_CATEGORIA[categoria] ?? categoria;
  }

  protected abrirNuevo(): void {
    this.editandoId.set(null);
    this.formulario = this.formularioVacio();
    this.activeOriginal = true;
    this.errorModal.set('');
    this.modalAbierto.set(true);
  }

  protected abrirEditar(cocktail: Cocktail): void {
    this.editandoId.set(cocktail.id);
    this.formulario = {
      name: cocktail.name,
      category: cocktail.category,
      ingredients: cocktail.ingredients,
      imageUrl: cocktail.imageUrl,
      active: cocktail.active,
      eventTypeIds: cocktail.eventTypes.map((t) => t.id),
    };
    this.activeOriginal = cocktail.active;
    this.errorModal.set('');
    this.modalAbierto.set(true);
  }

  protected cerrarModal(): void {
    if (this.guardando()) {
      return;
    }
    this.modalAbierto.set(false);
  }

  protected alternarEvento(id: number): void {
    const ids = this.formulario.eventTypeIds;
    this.formulario = {
      ...this.formulario,
      eventTypeIds: ids.includes(id) ? ids.filter((i) => i !== id) : [...ids, id],
    };
  }

  protected eventoMarcado(id: number): boolean {
    return this.formulario.eventTypeIds.includes(id);
  }

  protected guardar(): void {
    const nombre = this.formulario.name.trim();
    const ingredientes = this.formulario.ingredients.trim();

    if (!nombre || !ingredientes) {
      this.errorModal.set('El nombre y los ingredientes son obligatorios.');
      return;
    }

    this.guardando.set(true);
    this.errorModal.set('');

    const payload: CocktailPayload = {
      ...this.formulario,
      name: nombre,
      ingredients: ingredientes,
      imageUrl: this.formulario.imageUrl?.trim() || null,
      active: this.editandoId() === null ? true : this.formulario.active,
    };

    const peticion =
      this.editandoId() === null
        ? this.servicio.create(payload)
        : this.servicio.update(this.editandoId()!, payload);

    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modalAbierto.set(false);
        this.cargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorModal.set(this.mensajeError(err, 'No se pudo guardar el cóctel.'));
      },
    });
  }

  protected alternarActivo(cocktail: Cocktail): void {
    const payload: CocktailPayload = {
      name: cocktail.name,
      category: cocktail.category,
      ingredients: cocktail.ingredients,
      imageUrl: cocktail.imageUrl,
      active: !cocktail.active,
      eventTypeIds: cocktail.eventTypes.map((t) => t.id),
    };

    this.servicio.update(cocktail.id, payload).subscribe({
      next: () => this.cargar(),
      error: (err) => this.error.set(this.mensajeError(err, 'No se pudo cambiar el estado.')),
    });
  }

  protected eliminar(cocktail: Cocktail): void {
    if (!confirm(`¿Desactivar "${cocktail.name}"? (no se borra, solo se oculta)`)) {
      return;
    }

    this.servicio.deactivate(cocktail.id).subscribe({
      next: () => this.cargar(),
      error: (err) => this.error.set(this.mensajeError(err, 'No se pudo desactivar el cóctel.')),
    });
  }

  protected alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  protected cerrarSesion(): void {
    this.auth.logout();
    this.router.navigateByUrl('/');
  }

  private formularioVacio(): CocktailPayload {
    return {
      name: '',
      category: '',
      ingredients: '',
      imageUrl: null,
      active: true,
      eventTypeIds: [],
    };
  }

  private mensajeError(err: unknown, porDefecto: string): string {
    const mensaje = (err as { error?: { message?: string } })?.error?.message;
    return mensaje || porDefecto;
  }
}
