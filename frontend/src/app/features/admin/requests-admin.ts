import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { ResumenSolicitudes, Solicitud } from '../../core/models/solicitud';
import { AdminRequestsService } from '../../core/services/admin-requests';
import { AuthService } from '../../core/services/auth';
import { enlaceWhatsapp, mensajePaquete, telefonoLimpio } from '../../core/utils/whatsapp';

const POR_PAGINA = 10;

const ESTADOS = ['', 'PENDIENTE', 'CONFIRMADO', 'CANCELADO'];

@Component({
  imports: [FormsModule, RouterLink, RouterLinkActive],
  selector: 'app-requests-admin',
  styleUrl: './requests-admin.css',
  templateUrl: './requests-admin.html',
})
export class RequestsAdmin implements OnInit {
  private readonly servicio = inject(AdminRequestsService);
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  private temporizadorBusqueda: ReturnType<typeof setTimeout> | undefined;

  protected readonly solicitudes = signal<Solicitud[]>([]);
  protected readonly resumen = signal<ResumenSolicitudes | null>(null);
  protected readonly total = signal(0);
  protected readonly cargando = signal(true);
  protected readonly error = signal('');

  protected readonly busqueda = signal('');
  protected readonly estadoFiltro = signal('');
  protected readonly menuAbierto = signal(false);
  protected readonly pagina = signal(1);

  protected readonly estados = ESTADOS;

  protected readonly totalPaginas = computed(() =>
    Math.max(1, Math.ceil(this.total() / POR_PAGINA)),
  );

  protected readonly desde = computed(() =>
    this.total() === 0 ? 0 : (this.pagina() - 1) * POR_PAGINA + 1,
  );

  protected readonly hasta = computed(() =>
    Math.min(this.pagina() * POR_PAGINA, this.total()),
  );

  protected readonly totalSolicitudes = computed(() => this.resumen()?.solicitudes ?? 0);

  protected readonly ingresosEstimados = computed(
    () => this.resumen()?.ingresosEstimados ?? 0,
  );

  protected readonly proximosEventos = computed(() => this.resumen()?.proximosEventos ?? 0);

  protected readonly pendientes = computed(() => this.resumen()?.pendientes ?? 0);

  ngOnInit(): void {
    this.cargar();
    this.cargarResumen();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    this.servicio
      .page({
        page: this.pagina() - 1,
        size: POR_PAGINA,
        status: this.estadoFiltro(),
        search: this.busqueda(),
      })
      .subscribe({
        next: (pagina) => {
          if (pagina.items.length === 0 && this.pagina() > 1) {
            this.pagina.set(1);
            this.cargar();
            return;
          }
          this.solicitudes.set(pagina.items);
          this.total.set(pagina.total);
          this.cargando.set(false);
        },
        error: () => {
          this.error.set('No se pudieron cargar las solicitudes (¿está el gateway corriendo?)');
          this.cargando.set(false);
        },
      });
  }

  protected cargarResumen(): void {
    this.servicio.resumen().subscribe({
      next: (resumen) => this.resumen.set(resumen),
      error: () => this.resumen.set(null),
    });
  }

  protected cambiarBusqueda(texto: string): void {
    this.busqueda.set(texto);
    this.pagina.set(1);
    if (this.temporizadorBusqueda) {
      clearTimeout(this.temporizadorBusqueda);
    }
    this.temporizadorBusqueda = setTimeout(() => this.cargar(), 300);
  }

  protected filtrarPorEstado(estado: string): void {
    this.estadoFiltro.set(estado);
    this.pagina.set(1);
    this.cargar();
  }

  protected irAPagina(pagina: number): void {
    if (pagina >= 1 && pagina <= this.totalPaginas()) {
      this.pagina.set(pagina);
      this.cargar();
    }
  }

  protected verDetalle(solicitud: Solicitud): void {
    this.router.navigate(['/panel/solicitudes', solicitud.id]);
  }

  protected abrirWhatsApp(solicitud: Solicitud): void {
    window.open(
      enlaceWhatsapp(solicitud.client.whatsapp, mensajePaquete(solicitud)),
      '_blank',
      'noopener',
    );
  }

  protected tieneWhatsapp(solicitud: Solicitud): boolean {
    return telefonoLimpio(solicitud.client.whatsapp) !== '';
  }

  protected etiquetaEstado(estado: string): string {
    return estado.charAt(0) + estado.slice(1).toLowerCase();
  }

  protected dinero(valor: number): string {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN',
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    }).format(valor);
  }

  protected fechaCorta(fecha: string): string {
    const meses = [
      'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun',
      'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic',
    ];
    const [anio, mes, dia] = fecha.split('-');
    return `${dia} ${meses[Number(mes) - 1]}, ${anio}`;
  }

  protected alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  protected cerrarSesion(): void {
    this.auth.logout();
    this.router.navigateByUrl('/');
  }
}
