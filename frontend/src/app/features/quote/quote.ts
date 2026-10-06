import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Cocktail } from '../../core/models/cocktail';
import { Desglose, ServiceTier, Solicitud, SolicitudPayload } from '../../core/models/solicitud';
import { CatalogService } from '../../core/services/catalog';
import { SolicitudesService } from '../../core/services/solicitudes';
import { enlaceConDueno, mensajeParaDueno } from '../../core/utils/whatsapp';

export interface TipoEvento {
  id: number;
  code: string;
  name: string;
  description: string;
}

const ETIQUETAS_CATEGORIA: Record<string, string> = {
  SIGNATURE: 'Signature',
  CLASICOS: 'Clásicos',
  CITRICOS: 'Cítricos',
  SIN_ALCOHOL: 'Sin Alcohol',
};

const MESES = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre',
];

@Component({
  imports: [FormsModule, RouterLink],
  selector: 'app-quote',
  styleUrl: './quote.css',
  templateUrl: './quote.html',
})
export class Quote implements OnInit {
  private readonly catalogo = inject(CatalogService);
  private readonly solicitudes = inject(SolicitudesService);

  protected readonly paso = signal(1);
  protected readonly confirmada = signal<Solicitud | null>(null);

  protected readonly tiposEvento = signal<TipoEvento[]>([]);
  protected readonly cocktails = signal<Cocktail[]>([]);
  protected readonly tiers = signal<ServiceTier[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal('');

  protected readonly busqueda = signal('');
  protected readonly categorias = signal<string[]>([]);
  protected readonly seleccion = signal<number[]>([]);

  protected readonly enviando = signal(false);
  protected readonly errorEnvio = signal('');
  protected readonly enlaceCopiado = signal(false);

  protected readonly etiquetas = ETIQUETAS_CATEGORIA;

  protected eventTypeId: number | null = null;
  protected guests = 50;
  protected durationHours = 4;
  protected eventDate = '';
  protected eventTime = '';

  protected nivel: 'BASE' | 'PREMIUM' = 'BASE';
  protected bartendersExtra = 0;

  protected nombre = '';
  protected whatsapp = '';
  protected email = '';
  protected ubicacion = '';
  protected notas = '';

  protected validacionPaso1 = '';
  protected validacionPaso2 = '';
  protected validacionPaso3 = '';

  ngOnInit(): void {
    this.catalogo.getEventTypes().subscribe({
      next: (tipos) => {
        this.tiposEvento.set(tipos.filter((t) => t.active));
        if (this.tiposEvento().length > 0 && this.eventTypeId === null) {
          this.eventTypeId = this.tiposEvento()[0].id;
        }
      },
      error: () => this.error.set('No se pudieron cargar los tipos de evento.'),
    });

    this.catalogo.getCocktails().subscribe({
      next: (lista) => {
        this.cocktails.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la carta (¿está el gateway corriendo?)');
        this.cargando.set(false);
      },
    });

    this.solicitudes.getTiers().subscribe({
      next: (tiers) => this.tiers.set(tiers),
      error: () => this.tiers.set([]),
    });
  }

  protected precio(code: string, porDefecto: number): number {
    return this.tiers().find((t) => t.code === code)?.price ?? porDefecto;
  }

  protected desglose(): Desglose {
    const base = this.precio('BASE', 6) * this.guests * this.durationHours;
    const premium = this.nivel === 'PREMIUM' ? this.precio('PREMIUM', 6) * this.guests : 0;
    const personal = this.precio('PERSONAL', 25) * this.durationHours * this.bartendersExtra;
    const redondear = (v: number) => Math.round(v * 100) / 100;
    return {
      base: redondear(base),
      premium: redondear(premium),
      personal: redondear(personal),
      total: redondear(base + premium + personal),
    };
  }

  protected dinero(valor: number): string {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN',
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    }).format(valor);
  }

  protected pdfLink(solicitud: Solicitud): string {
    return `/api/public/solicitudes/${solicitud.publicToken}/pdf`;
  }

  protected linkSeguimiento(solicitud: Solicitud): string {
    const origen = typeof window === 'undefined' ? '' : window.location.origin;
    return `${origen}/solicitud/${solicitud.publicToken}`;
  }

  protected contactoWhatsapp(): string {
    return enlaceConDueno('Hola, escribo desde el sitio de Velvet & Gilt.');
  }

  protected paqueteWhatsapp(solicitud: Solicitud): string {
    return enlaceConDueno(mensajeParaDueno(solicitud));
  }

  protected copiarEnlace(solicitud: Solicitud): void {
    const enlace = this.linkSeguimiento(solicitud);
    const listo = () => {
      this.enlaceCopiado.set(true);
      setTimeout(() => this.enlaceCopiado.set(false), 2500);
    };
    if (navigator.clipboard?.writeText) {
      navigator.clipboard.writeText(enlace).then(listo);
      return;
    }
    window.prompt('Copia el link de seguimiento:', enlace);
  }

  protected fechaLegible(fecha: string): string {
    if (!fecha) {
      return 'Por definir';
    }
    const [anio, mes, dia] = fecha.split('-').map(Number);
    return `${dia} de ${MESES[mes - 1]}, ${anio}`;
  }

  protected fechaCorta(fecha: string): string {
    if (!fecha) {
      return '—';
    }
    const [anio, mes, dia] = fecha.split('-');
    return `${dia} ${MESES[Number(mes) - 1].slice(0, 3)}, ${anio}`;
  }

  protected nombreTipo(): TipoEvento | undefined {
    return this.tiposEvento().find((t) => t.id === this.eventTypeId);
  }

  protected visibles(): Cocktail[] {
    const texto = this.busqueda().trim().toLowerCase();
    const cats = new Set(this.categorias());
    const tipoId = this.eventTypeId;
    return this.cocktails().filter((c) => {
      if (!c.active) {
        return false;
      }
      const coincideTipo = tipoId === null || c.eventTypes.some((t) => t.id === tipoId);
      const coincideTexto =
        texto === '' ||
        c.name.toLowerCase().includes(texto) ||
        c.ingredients.toLowerCase().includes(texto);
      const coincideCategoria = cats.size === 0 || cats.has(c.category);
      return coincideTipo && coincideTexto && coincideCategoria;
    });
  }

  protected seleccionados(): Cocktail[] {
    const ids = new Set(this.seleccion());
    return this.cocktails().filter((c) => ids.has(c.id));
  }

  protected alternarCategoria(categoria: string): void {
    this.categorias.update((actual) =>
      actual.includes(categoria) ? actual.filter((c) => c !== categoria) : [...actual, categoria],
    );
  }

  protected categoriaMarcada(categoria: string): boolean {
    return this.categorias().includes(categoria);
  }

  protected alternarCoctel(id: number): void {
    this.seleccion.update((actual) =>
      actual.includes(id) ? actual.filter((c) => c !== id) : [...actual, id],
    );
    this.validacionPaso2 = '';
  }

  protected seleccionado(id: number): boolean {
    return this.seleccion().includes(id);
  }

  protected etiquetaCategoria(categoria: string): string {
    return ETIQUETAS_CATEGORIA[categoria] ?? categoria;
  }

  protected irAPaso(destino: number): void {
    if (destino >= this.paso()) {
      return;
    }
    this.paso.set(destino);
    window.scrollTo({ top: 0 });
  }

  protected siguienteDesdePaso1(): void {
    if (this.eventTypeId === null) {
      this.validacionPaso1 = 'Selecciona el tipo de evento.';
      return;
    }
    if (!Number.isFinite(this.guests) || this.guests < 1 || this.guests > 100000) {
      this.validacionPaso1 = 'Indica un número de invitados entre 1 y 100000.';
      return;
    }
    if (!Number.isFinite(this.durationHours) || this.durationHours < 1 || this.durationHours > 24) {
      this.validacionPaso1 = 'La duración debe estar entre 1 y 24 horas.';
      return;
    }
    if (!this.eventDate) {
      this.validacionPaso1 = 'Selecciona la fecha del evento.';
      return;
    }
    this.validacionPaso1 = '';
    this.paso.set(2);
    window.scrollTo({ top: 0 });
  }

  protected siguienteDesdePaso2(): void {
    if (this.seleccion().length === 0) {
      this.validacionPaso2 = 'Selecciona al menos un cóctel para la carta del evento.';
      return;
    }
    this.validacionPaso2 = '';
    this.paso.set(3);
    window.scrollTo({ top: 0 });
  }

  protected regresarAPaso2(): void {
    this.paso.set(2);
    window.scrollTo({ top: 0 });
  }

  protected emailValido(): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email.trim());
  }

  protected confirmar(): void {
    const limpio = (v: string) => v.trim();

    if (!limpio(this.nombre) || !limpio(this.whatsapp) || !limpio(this.email) || !limpio(this.ubicacion)) {
      this.validacionPaso3 = 'Completa nombre, WhatsApp, correo y ubicación del evento.';
      return;
    }
    if (!this.emailValido()) {
      this.validacionPaso3 = 'El correo electrónico no es válido.';
      return;
    }

    const payload: SolicitudPayload = {
      eventTypeId: this.eventTypeId!,
      eventDate: this.eventDate,
      eventTime: this.eventTime || null,
      guests: this.guests,
      durationHours: this.durationHours,
      location: limpio(this.ubicacion),
      notes: limpio(this.notas) || null,
      level: this.nivel,
      extraBartenders: this.bartendersExtra,
      cocktailIds: this.seleccion(),
      clientName: limpio(this.nombre),
      clientWhatsapp: limpio(this.whatsapp),
      clientEmail: limpio(this.email),
    };

    this.validacionPaso3 = '';
    this.errorEnvio.set('');
    this.enviando.set(true);

    this.solicitudes.crear(payload).subscribe({
      next: (solicitud) => {
        this.enviando.set(false);
        this.confirmada.set(solicitud);
        window.scrollTo({ top: 0 });
      },
      error: (err) => {
        this.enviando.set(false);
        this.errorEnvio.set(this.mensajeError(err, 'No se pudo enviar la solicitud. Intenta de nuevo.'));
      },
    });
  }

  protected reiniciar(): void {
    this.confirmada.set(null);
    this.paso.set(1);
    this.seleccion.set([]);
    this.categorias.set([]);
    this.busqueda.set('');
    this.nombre = '';
    this.whatsapp = '';
    this.email = '';
    this.ubicacion = '';
    this.notas = '';
    this.nivel = 'BASE';
    this.bartendersExtra = 0;
    this.validacionPaso1 = '';
    this.validacionPaso2 = '';
    this.validacionPaso3 = '';
    this.errorEnvio.set('');
    window.scrollTo({ top: 0 });
  }

  protected mailto(): string {
    const asunto = encodeURIComponent('Cotización de evento - Velvet & Gilt');
    const cuerpo = encodeURIComponent(
      [
        'Hola,',
        '',
        'Quisiera solicitar una cotización para un evento:',
        `Fecha: ${this.fechaLegible(this.eventDate)}`,
        `Tipo: ${this.nombreTipo()?.name ?? ''}`,
        `Invitados: ${this.guests}`,
        `Duración: ${this.durationHours} horas`,
        `Nivel: ${this.nivel === 'PREMIUM' ? 'Premium' : 'Base'}`,
        `Cócteles: ${this.seleccionados().map((c) => c.name).join(', ')}`,
        `Total estimado: ${this.dinero(this.desglose().total)}`,
        '',
        `Nombre: ${this.nombre}`,
        `WhatsApp: ${this.whatsapp}`,
        `Ubicación: ${this.ubicacion}`,
        '',
        'Gracias.',
      ].join('\n'),
    );
    return `mailto:${this.email.trim()}?subject=${asunto}&body=${cuerpo}`;
  }

  private mensajeError(err: unknown, porDefecto: string): string {
    const cuerpo = (err as { error?: { message?: string; fields?: Record<string, string> } })?.error;
    if (cuerpo?.fields) {
      const primera = Object.values(cuerpo.fields)[0];
      return primera || porDefecto;
    }
    return cuerpo?.message || porDefecto;
  }
}
