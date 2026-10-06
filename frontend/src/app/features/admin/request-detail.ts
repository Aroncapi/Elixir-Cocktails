import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { Solicitud } from '../../core/models/solicitud';
import { AdminRequestsService } from '../../core/services/admin-requests';
import { AuthService } from '../../core/services/auth';
import { enlaceWhatsapp, mensajePaquete, telefonoLimpio } from '../../core/utils/whatsapp';

const ESTADOS = ['PENDIENTE', 'CONFIRMADO', 'CANCELADO'];

@Component({
  imports: [RouterLink, RouterLinkActive],
  selector: 'app-request-detail',
  styleUrls: ['./requests-admin.css', './request-detail.css'],
  templateUrl: './request-detail.html',
})
export class RequestDetail implements OnInit {
  private readonly servicio = inject(AdminRequestsService);
  private readonly http = inject(HttpClient);
  private readonly ruta = inject(ActivatedRoute);
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly solicitud = signal<Solicitud | null>(null);
  protected readonly cargando = signal(true);
  protected readonly error = signal('');
  protected readonly guardando = signal(false);
  protected readonly pdfGenerando = signal(false);
  protected readonly menuAbierto = signal(false);
  protected readonly cambioEstado = signal(false);

  protected readonly estados = ESTADOS;

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id) || id <= 0) {
      this.error.set('Identificador de solicitud no valido.');
      this.cargando.set(false);
      return;
    }
    this.servicio.get(id).subscribe({
      next: (solicitud) => {
        this.solicitud.set(solicitud);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la solicitud (¿esta el gateway corriendo?).');
        this.cargando.set(false);
      },
    });
  }

  protected cambiarEstado(nuevo: string): void {
    const actual = this.solicitud();
    if (!actual || actual.status === nuevo || this.guardando()) {
      return;
    }
    this.guardando.set(true);
    this.servicio.updateStatus(actual.id, nuevo).subscribe({
      next: (actualizada) => {
        this.solicitud.set(actualizada);
        this.guardando.set(false);
        this.cambioEstado.set(false);
      },
      error: (err) => {
        const mensaje = err?.error?.message;
        this.error.set(
          mensaje
            ? `No se pudo actualizar el estado: ${mensaje}`
            : 'No se pudo actualizar el estado de la solicitud.',
        );
        this.guardando.set(false);
      },
    });
  }

  protected alternarCambioEstado(): void {
    this.cambioEstado.update((abierto) => !abierto);
  }

  protected descargarPdf(): void {
    const solicitud = this.solicitud();
    if (!solicitud || this.pdfGenerando()) {
      return;
    }
    this.pdfGenerando.set(true);
    this.http.get(`/api/requests/${solicitud.id}/pdf`, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `${solicitud.folio}.pdf`;
        enlace.click();
        URL.revokeObjectURL(url);
        this.pdfGenerando.set(false);
      },
      error: () => {
        this.error.set('No se pudo generar el PDF (¿esta el gateway corriendo?).');
        this.pdfGenerando.set(false);
      },
    });
  }

  protected abrirWhatsApp(): void {
    const solicitud = this.solicitud();
    if (!solicitud) {
      return;
    }
    window.open(
      enlaceWhatsapp(solicitud.client.whatsapp, mensajePaquete(solicitud)),
      '_blank',
      'noopener',
    );
  }

  protected tieneWhatsapp(): boolean {
    const solicitud = this.solicitud();
    return solicitud ? telefonoLimpio(solicitud.client.whatsapp) !== '' : false;
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

  protected fechaLarga(fecha: string): string {
    const meses = [
      'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
      'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre',
    ];
    const [anio, mes, dia] = fecha.split('-');
    return `${dia} de ${meses[Number(mes) - 1]}, ${anio}`;
  }

  protected fechaHora(): string {
    const solicitud = this.solicitud();
    if (!solicitud) {
      return '';
    }
    const fecha = this.fechaLarga(solicitud.eventDate);
    return solicitud.eventTime ? `${fecha} • ${solicitud.eventTime} hrs` : fecha;
  }

  protected nivelTexto(): string {
    return this.solicitud()?.level === 'PREMIUM' ? 'Menu Premium' : 'Servicio Base';
  }

  protected alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  protected cerrarSesion(): void {
    this.auth.logout();
    this.router.navigateByUrl('/');
  }
}
