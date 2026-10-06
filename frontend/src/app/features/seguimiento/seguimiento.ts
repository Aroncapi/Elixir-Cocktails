import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Solicitud } from '../../core/models/solicitud';
import { SolicitudesService } from '../../core/services/solicitudes';
import { enlaceConDueno, mensajeParaDueno } from '../../core/utils/whatsapp';

const MESES = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre',
];

@Component({
  imports: [RouterLink],
  selector: 'app-seguimiento',
  styleUrl: './seguimiento.css',
  templateUrl: './seguimiento.html',
})
export class Seguimiento implements OnInit {
  private readonly solicitudes = inject(SolicitudesService);
  private readonly ruta = inject(ActivatedRoute);

  protected readonly solicitud = signal<Solicitud | null>(null);
  protected readonly cargando = signal(true);
  protected readonly error = signal('');

  ngOnInit(): void {
    const token = this.ruta.snapshot.paramMap.get('token') ?? '';
    if (!token) {
      this.error.set('El enlace de seguimiento no es valido.');
      this.cargando.set(false);
      return;
    }

    this.solicitudes.porToken(token).subscribe({
      next: (solicitud) => {
        this.solicitud.set(solicitud);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No encontramos ninguna solicitud con este enlace. Verifica que se haya copiado completo.');
        this.cargando.set(false);
      },
    });
  }

  protected etiquetaEstado(estado: string): string {
    return estado.charAt(0) + estado.slice(1).toLowerCase();
  }

  protected resumenEstado(): string {
    switch (this.solicitud()?.status) {
      case 'CONFIRMADO':
        return 'Tu solicitud esta confirmada. Nuestro equipo ya tiene tu evento en el calendario.';
      case 'CANCELADO':
        return 'Tu solicitud fue cancelada. Si tienes dudas, escribenos por WhatsApp.';
      default:
        return 'Tu solicitud esta en revision. Un concierge te contactara en menos de 24 horas.';
    }
  }

  protected dinero(valor: number): string {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN',
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    }).format(valor);
  }

  protected fechaLegible(fecha: string): string {
    if (!fecha) {
      return 'Por definir';
    }
    const [anio, mes, dia] = fecha.split('-');
    return `${Number(dia)} de ${MESES[Number(mes) - 1]}, ${anio}`;
  }

  protected pdfLink(): string {
    const solicitud = this.solicitud();
    return solicitud ? `/api/public/solicitudes/${solicitud.publicToken}/pdf` : '#';
  }

  protected whatsappLink(): string {
    const solicitud = this.solicitud();
    if (!solicitud) {
      return 'https://wa.me/';
    }
    return enlaceConDueno(mensajeParaDueno(solicitud));
  }
}
