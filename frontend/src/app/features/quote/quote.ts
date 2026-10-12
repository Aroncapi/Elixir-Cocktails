import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Cocktail } from '../../core/models/cocktail';
import { DESTILADOS, ETIQUETAS_DESTILADO } from '../../core/models/spirits';
import { Desglose, ServiceTier, Solicitud, SolicitudPayload } from '../../core/models/solicitud';
import { CatalogService } from '../../core/services/catalog';
import { SettingsService } from '../../core/services/settings';
import { SolicitudesService } from '../../core/services/solicitudes';
import { montoDoble } from '../../core/utils/dinero';
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
};

const MESES = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre',
];

/** Primer numero de invitados en el que entra cada bartender adicional. */
const BARTENDER_STEPS = [55, 120, 180, 231, 271, 331, 371, 431, 481];
/** Invitados que suma cada bartender adicional despues de la tabla. */
const BARTENDER_STEP_TAIL = 50;
/** Primer numero de invitados en el que entra cada ayudante adicional. */
const ASSISTANT_STEPS = [100, 150, 271, 371, 481];
/** Invitados que suma cada ayudante adicional despues de la tabla. */
const ASSISTANT_STEP_TAIL = 100;

/** Cuenta cuantos umbrales alcanza el numero de invitados (+ base inicial). */
function contar(guests: number, steps: number[], tail: number, base: number): number {
  let total = base;
  let ultimoUmbral = 0;
  for (const umbral of steps) {
    if (guests >= umbral) {
      total++;
      ultimoUmbral = umbral;
    } else {
      break;
    }
  }
  if (ultimoUmbral === steps[steps.length - 1]) {
    total += Math.floor((guests - ultimoUmbral) / tail);
  }
  return total;
}

@Component({
  imports: [FormsModule, RouterLink],
  selector: 'app-quote',
  styleUrl: './quote.css',
  templateUrl: './quote.html',
})
export class Quote implements OnInit {
  private readonly catalogo = inject(CatalogService);
  private readonly solicitudes = inject(SolicitudesService);
  protected readonly settings = inject(SettingsService);

  protected readonly paso = signal(1);
  protected readonly confirmada = signal<Solicitud | null>(null);

  protected readonly tiposEvento = signal<TipoEvento[]>([]);
  protected readonly cocktails = signal<Cocktail[]>([]);
  protected readonly tiers = signal<ServiceTier[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal('');

  protected readonly busqueda = signal('');
  protected readonly categorias = signal<string[]>([]);
  protected readonly destilados = signal<string[]>([]);
  protected readonly seleccion = signal<number[]>([]);

  protected readonly enviando = signal(false);
  protected readonly errorEnvio = signal('');
  protected readonly enlaceCopiado = signal(false);

  protected readonly etiquetas = ETIQUETAS_CATEGORIA;
  protected readonly etiquetasDestilado = ETIQUETAS_DESTILADO;
  protected readonly destiladosDisponibles = DESTILADOS;

  protected eventTypeId: number | null = null;
  protected guests = 50;
  protected durationHours = 4;
  protected eventDate = '';
  protected eventTime = '';

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

  /** Horas realmente cobradas: siempre el minimo del paquete base. */
  protected horasCobradas(): number {
    return Math.max(this.durationHours, this.settings.horasBase());
  }

  /** Equipo asignado automaticamente segun el numero de invitados. */
  protected equipo(): { bartenders: number; assistants: number } {
    const masterclass = this.nombreTipo()?.code === 'MASTERCLASS';
    if (masterclass) {
      return { bartenders: 1, assistants: this.guests >= 60 ? 1 : 0 };
    }
    return {
      bartenders: contar(this.guests, BARTENDER_STEPS, BARTENDER_STEP_TAIL, 1),
      assistants: contar(this.guests, ASSISTANT_STEPS, ASSISTANT_STEP_TAIL, 0),
    };
  }

  protected desglose(): Desglose {
    const horasExtra = this.horasCobradas() - this.settings.horasBase();
    const extra = this.settings.horaExtra();
    const { bartenders, assistants } = this.equipo();

    const porBartender = this.precio('BASE', 272) + extra * horasExtra;
    const porAyudante = this.precio('PERSONAL', 150) + extra * horasExtra;

    const base = porBartender * bartenders;
    const personal = porAyudante * assistants;
    const redondear = (v: number) => Math.round(v * 100) / 100;
    return {
      base: redondear(base),
      premium: 0,
      personal: redondear(personal),
      total: redondear(base + personal),
    };
  }

  protected dinero(valor: number): string {
    return montoDoble(valor);
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

  /** Aplica todos los filtros del sidebar, sin considerar el tipo de evento. */
  protected filtrados(): Cocktail[] {
    const texto = this.busqueda().trim().toLowerCase();
    const cats = new Set(this.categorias());
    const spirts = new Set(this.destilados());
    return this.cocktails().filter((c) => {
      if (!c.active) {
        return false;
      }
      const coincideTexto =
        texto === '' ||
        c.name.toLowerCase().includes(texto) ||
        c.ingredients.toLowerCase().includes(texto);
      const coincideCategoria = cats.size === 0 || cats.has(c.category);
      const coincideDestilado = spirts.size === 0 || (c.spirit !== null && spirts.has(c.spirit));
      return coincideTexto && coincideCategoria && coincideDestilado;
    });
  }

  /** Recomendados para el tipo de evento elegido. */
  protected recomendados(): Cocktail[] {
    const tipoId = this.eventTypeId;
    return this.filtrados().filter(
      (c) => tipoId === null || c.eventTypes.some((t) => t.id === tipoId),
    );
  }

  /** El resto de la carta (lo que no va recomendado para ese evento). */
  protected resto(): Cocktail[] {
    const tipoId = this.eventTypeId;
    return this.filtrados().filter(
      (c) => tipoId !== null && !c.eventTypes.some((t) => t.id === tipoId),
    );
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

  protected alternarDestilado(destilado: string): void {
    this.destilados.update((actual) =>
      actual.includes(destilado) ? actual.filter((d) => d !== destilado) : [...actual, destilado],
    );
  }

  protected destiladoMarcado(destilado: string): boolean {
    return this.destilados().includes(destilado);
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

  protected etiquetaDestilado(destilado: string | null): string {
    if (!destilado) {
      return '';
    }
    return ETIQUETAS_DESTILADO[destilado] ?? destilado;
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
      level: 'BASE',
      extraBartenders: 0,
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
    this.destilados.set([]);
    this.busqueda.set('');
    this.nombre = '';
    this.whatsapp = '';
    this.email = '';
    this.ubicacion = '';
    this.notas = '';
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
        `Duracion: ${this.horasCobradas()} horas`,
        `Equipo: ${this.equipo().bartenders} bartender(s), ${this.equipo().assistants} ayudante(s)`,
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
