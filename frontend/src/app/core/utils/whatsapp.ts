import { Solicitud } from '../models/solicitud';
import { montoDoble, soles } from './dinero';

export const WHATSAPP_OWNER = '51937336603';

let dueno = WHATSAPP_OWNER;

export function setDuenoWhatsapp(numero: string | null | undefined): void {
  const limpio = telefonoLimpio(numero);
  if (limpio.length >= 8) {
    dueno = limpio;
  }
}

export function duenoWhatsapp(): string {
  return dueno;
}

const MESES = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre',
];

export function telefonoLimpio(telefono: string | null | undefined): string {
  return (telefono ?? '').replace(/[^\d]/g, '');
}

export function dineroPEN(valor: number): string {
  return soles(valor);
}

export function fechaLarga(fecha: string): string {
  if (!fecha) {
    return 'Por definir';
  }
  const [anio, mes, dia] = fecha.split('-');
  return `${Number(dia)} de ${MESES[Number(mes) - 1]}, ${anio}`;
}

export function mensajePaquete(solicitud: Solicitud): string {
  const carta = solicitud.cocktails.map((coctel) => coctel.name).join(', ');
  const enlace = `${window.location.origin}/solicitud/${solicitud.publicToken}`;
  const hora = solicitud.eventTime ? `, ${solicitud.eventTime} hrs` : '';

  return [
    `Hola ${solicitud.client.name}, aqui esta tu paquete para ${solicitud.eventTypeName} (${solicitud.folio}):`,
    '',
    `Fecha: ${fechaLarga(solicitud.eventDate)}${hora}`,
    `Lugar: ${solicitud.location ?? 'Por definir'}`,
    `Invitados: ${solicitud.guests} | Duracion: ${solicitud.durationHours} h`,
    `Equipo: ${solicitud.bartenders} bartender(s), ${solicitud.assistants} ayudante(s)`,
    `Carta: ${carta}`,
    '',
    `Bartenders: ${dineroPEN(solicitud.breakdown.base)}`,
    `Premium: ${dineroPEN(solicitud.breakdown.premium)}`,
    `Ayudantes: ${dineroPEN(solicitud.breakdown.personal)}`,
    `Total: ${montoDoble(solicitud.breakdown.total)}`,
    '',
    `Estado: ${solicitud.status.charAt(0) + solicitud.status.slice(1).toLowerCase()}`,
    `Seguimiento: ${enlace}`,
  ].join('\n');
}

export function mensajeParaDueno(solicitud: Solicitud): string {
  const carta = solicitud.cocktails.map((coctel) => coctel.name).join(', ');
  const enlace = `${window.location.origin}/solicitud/${solicitud.publicToken}`;
  const hora = solicitud.eventTime ? `, ${solicitud.eventTime} hrs` : '';

  return [
    `Hola, soy ${solicitud.client.name} y acabo de generar mi paquete (${solicitud.folio}) desde el sitio.`,
    '',
    `Evento: ${solicitud.eventTypeName}`,
    `Fecha: ${fechaLarga(solicitud.eventDate)}${hora}`,
    `Lugar: ${solicitud.location ?? 'Por definir'}`,
    `Invitados: ${solicitud.guests} | Duracion: ${solicitud.durationHours} h`,
    `Equipo: ${solicitud.bartenders} bartender(s), ${solicitud.assistants} ayudante(s)`,
    `Carta: ${carta}`,
    '',
    `Total: ${montoDoble(solicitud.breakdown.total)}`,
    `Seguimiento: ${enlace}`,
  ].join('\n');
}

export function enlaceWhatsapp(telefono: string | null | undefined, mensaje: string): string {
  const limpio = telefonoLimpio(telefono);
  const texto = encodeURIComponent(mensaje);
  return limpio ? `https://wa.me/${limpio}?text=${texto}` : `https://wa.me/?text=${texto}`;
}

export function enlaceConDueno(mensaje: string): string {
  return enlaceWhatsapp(dueno, mensaje);
}
