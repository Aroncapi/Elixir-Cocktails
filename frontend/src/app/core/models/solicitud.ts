export interface SolicitudPayload {
  eventTypeId: number;
  eventDate: string;
  eventTime: string | null;
  guests: number;
  durationHours: number;
  location: string | null;
  notes: string | null;
  level: 'BASE' | 'PREMIUM';
  extraBartenders: number;
  cocktailIds: number[];
  clientName: string;
  clientWhatsapp: string;
  clientEmail: string;
}

export interface Desglose {
  base: number;
  premium: number;
  personal: number;
  total: number;
}

export interface CocktailRef {
  id: number;
  name: string;
  ingredients: string;
  imageUrl: string | null;
  category: string;
}

export interface ClienteRef {
  id: number | null;
  name: string;
  email: string;
  whatsapp: string;
}

export interface Solicitud {
  id: number;
  folio: string;
  publicToken: string;
  status: 'PENDIENTE' | 'CONFIRMADO' | 'CANCELADO';
  eventTypeId: number;
  eventTypeName: string;
  eventDate: string;
  eventTime: string | null;
  guests: number;
  durationHours: number;
  location: string | null;
  notes: string | null;
  level: 'BASE' | 'PREMIUM';
  extraBartenders: number;
  breakdown: Desglose;
  cocktails: CocktailRef[];
  client: ClienteRef;
  createdAt: string;
}

export interface ServiceTier {
  id: number;
  code: string;
  name: string;
  description: string;
  tierType: string;
  price: number;
  active: boolean;
}

export interface SolicitudPage {
  items: Solicitud[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}

export interface ResumenSolicitudes {
  solicitudes: number;
  ingresosEstimados: number;
  proximosEventos: number;
  pendientes: number;
  confirmadas: number;
  canceladas: number;
}

export interface EventoCalendario {
  id: number;
  folio: string;
  eventDate: string;
  eventTime: string | null;
  status: 'PENDIENTE' | 'CONFIRMADO' | 'CANCELADO';
  eventTypeName: string;
  clientName: string;
  location: string | null;
  totalAmount: number;
}
