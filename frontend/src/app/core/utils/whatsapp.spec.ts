import { Solicitud } from '../models/solicitud';
import {
  WHATSAPP_OWNER,
  enlaceConDueno,
  enlaceWhatsapp,
  mensajePaquete,
  mensajeParaDueno,
  setDuenoWhatsapp,
  telefonoLimpio,
} from './whatsapp';

function solicitudEjemplo(): Solicitud {
  return {
    id: 7,
    folio: 'REQ-007',
    publicToken: 'abc123',
    status: 'PENDIENTE',
    eventTypeId: 1,
    eventTypeName: 'Bodas',
    eventDate: '2026-11-20',
    eventTime: '18:00',
    guests: 80,
    durationHours: 5,
    location: 'Polanco, CDMX',
    notes: null,
    level: 'PREMIUM',
    extraBartenders: 2,
    breakdown: { base: 2400, premium: 480, personal: 250, total: 3130 },
    cocktails: [
      { id: 1, name: 'Margarita de Bergamota', ingredients: '', imageUrl: null, category: 'SIGNATURE' },
      { id: 2, name: 'Paloma Rosa', ingredients: '', imageUrl: null, category: 'CITRICOS' },
    ],
    client: { id: 1, name: 'Ana Lopez', email: 'ana@ejemplo.mx', whatsapp: '+52 55 1234 5678' },
    createdAt: '2026-10-05T21:00:00Z',
  };
}

describe('whatsapp utils', () => {
  it('should strip everything but digits', () => {
    expect(telefonoLimpio('+52 55 1234-5678')).toBe('525512345678');
    expect(telefonoLimpio(null)).toBe('');
  });

  it('should build a wa.me link with the encoded message', () => {
    const enlace = enlaceWhatsapp('+52 55 1234 5678', 'hola mundo');
    expect(enlace.startsWith('https://wa.me/525512345678?text=')).toBe(true);
    expect(enlace).toContain('hola%20mundo');
  });

  it('should fall back to the chat picker when there is no phone', () => {
    expect(enlaceWhatsapp('', 'hola')).toBe('https://wa.me/?text=hola');
  });

  it('should include the package data in the message', () => {
    const mensaje = mensajePaquete(solicitudEjemplo());
    expect(mensaje).toContain('REQ-007');
    expect(mensaje).toContain('Ana Lopez');
    expect(mensaje).toContain('Bodas');
    expect(mensaje).toContain('20 de Noviembre, 2026');
    expect(mensaje).toContain('Polanco, CDMX');
    expect(mensaje).toContain('Margarita de Bergamota, Paloma Rosa');
    expect(mensaje).toContain('Menu Premium');
    expect(mensaje).toContain('$3,130');
    expect(mensaje).toContain('/solicitud/abc123');
  });

  it('should point to the owner whatsapp number', () => {
    expect(WHATSAPP_OWNER).toBe('51937336603');
    expect(enlaceConDueno('hola')).toBe('https://wa.me/51937336603?text=hola');
  });

  it('should build a client-to-owner message with the package data', () => {
    const mensaje = mensajeParaDueno(solicitudEjemplo());
    expect(mensaje).toContain('Hola, soy Ana Lopez');
    expect(mensaje).toContain('REQ-007');
    expect(mensaje).toContain('Bodas');
    expect(mensaje).toContain('20 de Noviembre, 2026');
    expect(mensaje).toContain('Polanco, CDMX');
    expect(mensaje).toContain('$3,130');
    expect(mensaje).toContain('/solicitud/abc123');
    expect(mensaje.startsWith('Hola Ana Lopez')).toBe(false);
  });

  it('should change the owner number at runtime (panel de configuracion)', () => {
    setDuenoWhatsapp('+51 999 999 999');
    expect(enlaceConDueno('hola')).toBe('https://wa.me/51999999999?text=hola');

    setDuenoWhatsapp(WHATSAPP_OWNER);
    expect(enlaceConDueno('hola')).toBe('https://wa.me/51937336603?text=hola');
  });

  it('should ignore an invalid owner number', () => {
    setDuenoWhatsapp('12');
    expect(enlaceConDueno('hola')).toBe('https://wa.me/51937336603?text=hola');
  });
});
