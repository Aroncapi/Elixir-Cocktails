import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { EventoCalendario } from '../../core/models/solicitud';
import { AdminRequestsService } from '../../core/services/admin-requests';
import { AuthService } from '../../core/services/auth';

const DIAS = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];

const MESES = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre',
];

const ESTADOS = ['', 'PENDIENTE', 'CONFIRMADO', 'CANCELADO'];

export interface DiaCalendario {
  fecha: string;
  numero: number;
  esHoy: boolean;
  eventos: EventoCalendario[];
}

@Component({
  imports: [RouterLink, RouterLinkActive],
  selector: 'app-calendario',
  styleUrls: ['./requests-admin.css', './calendario.css'],
  templateUrl: './calendario.html',
})
export class Calendario implements OnInit {
  private readonly servicio = inject(AdminRequestsService);
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);

  protected readonly mes = signal(new Date());
  protected readonly eventos = signal<EventoCalendario[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal('');
  protected readonly estadoFiltro = signal('');
  protected readonly menuAbierto = signal(false);

  protected readonly dias = DIAS;
  protected readonly estados = ESTADOS;

  protected readonly titulo = computed(() => {
    const fecha = this.mes();
    return `${MESES[fecha.getMonth()]} ${fecha.getFullYear()}`;
  });

  protected readonly calendario = computed<DiaCalendario[]>(() => {
    const base = this.mes();
    const anio = base.getFullYear();
    const mes = base.getMonth();
    const primerDia = new Date(anio, mes, 1).getDay();
    const totalDias = new Date(anio, mes + 1, 0).getDate();
    const hoy = this.hoy();

    const porFecha = new Map<string, EventoCalendario[]>();
    for (const evento of this.eventos()) {
      if (this.estadoFiltro() !== '' && evento.status !== this.estadoFiltro()) {
        continue;
      }
      const lista = porFecha.get(evento.eventDate) ?? [];
      lista.push(evento);
      porFecha.set(evento.eventDate, lista);
    }

    const celdas: DiaCalendario[] = [];
    for (let i = 0; i < primerDia; i++) {
      celdas.push({ fecha: `prev-${i}`, numero: 0, esHoy: false, eventos: [] });
    }
    for (let dia = 1; dia <= totalDias; dia++) {
      const fecha = this.stringify(new Date(anio, mes, dia));
      celdas.push({
        fecha,
        numero: dia,
        esHoy: fecha === hoy,
        eventos: porFecha.get(fecha) ?? [],
      });
    }
    while (celdas.length % 7 !== 0) {
      const i = celdas.length;
      celdas.push({ fecha: `next-${i}`, numero: 0, esHoy: false, eventos: [] });
    }
    return celdas;
  });

  protected readonly totalEventos = computed(
    () => this.calendario().reduce((suma, dia) => suma + dia.eventos.length, 0),
  );

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    const base = this.mes();
    const desde = new Date(base.getFullYear(), base.getMonth(), 1);
    const hasta = new Date(base.getFullYear(), base.getMonth() + 1, 0);

    this.servicio.eventos(this.stringify(desde), this.stringify(hasta)).subscribe({
      next: (eventos) => {
        this.eventos.set(eventos);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los eventos (¿está el gateway corriendo?)');
        this.cargando.set(false);
      },
    });
  }

  protected anterior(): void {
    this.mes.update((fecha) => new Date(fecha.getFullYear(), fecha.getMonth() - 1, 1));
    this.cargar();
  }

  protected siguiente(): void {
    this.mes.update((fecha) => new Date(fecha.getFullYear(), fecha.getMonth() + 1, 1));
    this.cargar();
  }

  protected irAHoy(): void {
    const hoy = new Date();
    this.mes.set(new Date(hoy.getFullYear(), hoy.getMonth(), 1));
    this.cargar();
  }

  protected filtrarPorEstado(estado: string): void {
    this.estadoFiltro.set(estado);
  }

  protected verDetalle(evento: EventoCalendario): void {
    this.router.navigate(['/panel/solicitudes', evento.id]);
  }

  protected etiquetaEstado(estado: string): string {
    return estado.charAt(0) + estado.slice(1).toLowerCase();
  }

  protected hora(evento: EventoCalendario): string {
    return evento.eventTime ?? '';
  }

  protected dinero(valor: number): string {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN',
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    }).format(valor);
  }

  protected alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  protected cerrarSesion(): void {
    this.auth.logout();
    this.router.navigateByUrl('/');
  }

  private hoy(): string {
    return this.stringify(new Date());
  }

  private stringify(fecha: Date): string {
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${fecha.getFullYear()}-${mes}-${dia}`;
  }
}
