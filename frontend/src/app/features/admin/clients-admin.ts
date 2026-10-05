import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Cliente } from '../../core/models/client';
import { AdminClientsService, ClientePayload } from '../../core/services/admin-clients';
import { AuthService } from '../../core/services/auth';

const ETIQUETAS_TIER: Record<string, string> = {
  VIP: 'VIP',
  CORPORATIVO: 'Corporativo',
  PARTICULAR: 'Particular',
};

const POR_PAGINA = 10;

@Component({
  imports: [FormsModule, RouterLink],
  selector: 'app-clients-admin',
  styleUrl: './clients-admin.css',
  templateUrl: './clients-admin.html',
})
export class ClientsAdmin implements OnInit {
  private readonly servicio = inject(AdminClientsService);
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal('');

  protected readonly busqueda = signal('');
  protected readonly tierFiltro = signal('');
  protected readonly pagina = signal(1);

  protected readonly menuAbierto = signal(false);
  protected readonly modalAbierto = signal(false);
  protected readonly guardando = signal(false);
  protected readonly errorModal = signal('');
  protected readonly editandoId = signal<number | null>(null);
  protected readonly seleccionado = signal<Cliente | null>(null);

  protected readonly etiquetas = ETIQUETAS_TIER;
  protected readonly tiers = ['', 'VIP', 'CORPORATIVO', 'PARTICULAR'];
  protected readonly porPagina = POR_PAGINA;

  protected readonly formulario: ClientePayload = this.formularioVacio();

  protected readonly visibles = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    const tier = this.tierFiltro();
    return this.clientes().filter((c) => {
      const coincideTier = tier === '' || c.tier === tier;
      const coincideTexto =
        texto === '' ||
        c.name.toLowerCase().includes(texto) ||
        (c.email ?? '').toLowerCase().includes(texto) ||
        (c.location ?? '').toLowerCase().includes(texto) ||
        (c.lastEvent ?? '').toLowerCase().includes(texto);
      return coincideTier && coincideTexto;
    });
  });

  protected readonly totalPaginas = computed(() =>
    Math.max(1, Math.ceil(this.visibles().length / POR_PAGINA))
  );

  protected readonly paginaClientes = computed(() => {
    const inicio = (this.pagina() - 1) * POR_PAGINA;
    return this.visibles().slice(inicio, inicio + POR_PAGINA);
  });

  protected readonly desde = computed(() =>
    this.visibles().length === 0 ? 0 : (this.pagina() - 1) * POR_PAGINA + 1
  );

  protected readonly hasta = computed(() =>
    Math.min(this.pagina() * POR_PAGINA, this.visibles().length)
  );

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    this.servicio.list().subscribe({
      next: (lista) => {
        this.clientes.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la cartera de clientes (¿está el gateway corriendo?)');
        this.cargando.set(false);
      },
    });
  }

  protected filtrarPorTier(tier: string): void {
    this.tierFiltro.set(tier);
    this.pagina.set(1);
  }

  protected cambiarBusqueda(texto: string): void {
    this.busqueda.set(texto);
    this.pagina.set(1);
  }

  protected irAPagina(pagina: number): void {
    if (pagina >= 1 && pagina <= this.totalPaginas()) {
      this.pagina.set(pagina);
    }
  }

  protected etiquetaTier(tier: string): string {
    return ETIQUETAS_TIER[tier] ?? tier;
  }

  protected iniciales(nombre: string): string {
    return nombre
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map((parte) => parte.charAt(0))
      .join('')
      .toUpperCase();
  }

  protected dinero(valor: number): string {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN',
      maximumFractionDigits: 0,
    }).format(valor);
  }

  protected abrirDetalles(cliente: Cliente): void {
    this.editandoId.set(cliente.id);
    this.seleccionado.set(cliente);
    this.formulario.name = cliente.name;
    this.formulario.email = cliente.email;
    this.formulario.whatsapp = cliente.whatsapp;
    this.formulario.location = cliente.location;
    this.formulario.tier = cliente.tier;
    this.formulario.totalSpent = cliente.totalSpent;
    this.formulario.active = cliente.active;
    this.errorModal.set('');
    this.modalAbierto.set(true);
  }

  protected cerrarModal(): void {
    if (this.guardando()) {
      return;
    }
    this.modalAbierto.set(false);
    this.seleccionado.set(null);
  }

  protected guardar(): void {
    const nombre = this.formulario.name.trim();
    if (!nombre) {
      this.errorModal.set('El nombre del cliente es obligatorio.');
      return;
    }

    this.guardando.set(true);
    this.errorModal.set('');

    const payload: ClientePayload = {
      name: nombre,
      email: this.formulario.email?.trim() || null,
      whatsapp: this.formulario.whatsapp?.trim() || null,
      location: this.formulario.location?.trim() || null,
      tier: this.formulario.tier,
      totalSpent: this.formulario.totalSpent ?? 0,
      active: this.formulario.active,
    };

    this.servicio.update(this.editandoId()!, payload).subscribe({
      next: () => {
        this.guardando.set(false);
        this.modalAbierto.set(false);
        this.seleccionado.set(null);
        this.cargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorModal.set(this.mensajeError(err, 'No se pudo guardar el cliente.'));
      },
    });
  }

  protected desactivar(cliente: Cliente): void {
    if (!confirm(`¿Desactivar "${cliente.name}"? (no se borra, solo se oculta)`)) {
      return;
    }

    this.servicio.deactivate(cliente.id).subscribe({
      next: () => {
        this.modalAbierto.set(false);
        this.seleccionado.set(null);
        this.cargar();
      },
      error: (err) => this.error.set(this.mensajeError(err, 'No se pudo desactivar el cliente.')),
    });
  }

  protected alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  protected cerrarSesion(): void {
    this.auth.logout();
    this.router.navigateByUrl('/');
  }

  private formularioVacio(): ClientePayload {
    return {
      name: '',
      email: null,
      whatsapp: null,
      location: null,
      tier: 'PARTICULAR',
      totalSpent: 0,
      active: true,
    };
  }

  private mensajeError(err: unknown, porDefecto: string): string {
    const mensaje = (err as { error?: { message?: string } })?.error?.message;
    return mensaje || porDefecto;
  }
}
