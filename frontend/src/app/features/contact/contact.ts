import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SettingsService } from '../../core/services/settings';
import { enlaceConDueno, fechaLarga } from '../../core/utils/whatsapp';

@Component({
  imports: [FormsModule],
  selector: 'app-contact',
  styleUrl: './contact.css',
  templateUrl: './contact.html',
})
export class Contact {
  protected readonly settings = inject(SettingsService);

  protected readonly fondoInfo =
    'https://lh3.googleusercontent.com/aida-public/AB6AXuDnBeP5f8Se0UXD126CYziGZMbZa9kJFjXuTxlplYE31aqNlTt9cnUSUDc4ByBwOVsIlwe3vzDgjE60pkoNNUL1aBe-V7VcdvwWF1b4L1cG9dTYYuc3Q87rpVBx9pZjQhZBD4rUc0yuZBSMJjRIteD2oOT9_G26y5uRxyFFODLBzUfjz1sLTnLkZ_mTyBXv9WI3ZW_2wqdXkTHmhanlV7aGpIc6HdnWSgQr7xYYW3RoyyzuQbVDM8Ro';

  protected nombre = '';
  protected email = '';
  protected fecha = '';
  protected mensaje = '';

  protected readonly error = signal('');

  protected emailValido(): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email.trim());
  }

  protected enviar(): void {
    if (!this.nombre.trim() || !this.email.trim()) {
      this.error.set('Completa tu nombre y tu correo.');
      return;
    }
    if (!this.emailValido()) {
      this.error.set('El correo electronico no es valido.');
      return;
    }

    const negocio = this.settings.nombreNegocio() || 'Velvet & Gilt';
    const cuerpo = [
      `Hola, escribo desde la pagina de ${negocio}.`,
      '',
      `Nombre: ${this.nombre.trim()}`,
      `Correo: ${this.email.trim()}`,
      ...(this.fecha ? [`Fecha del evento: ${fechaLarga(this.fecha)}`] : []),
      ...(this.mensaje.trim() ? ['', `Detalles: ${this.mensaje.trim()}`] : []),
    ].join('\n');

    this.error.set('');
    window.open(enlaceConDueno(cuerpo), '_blank', 'noopener');
  }
}
