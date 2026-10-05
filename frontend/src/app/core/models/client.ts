export type TipoCliente = 'VIP' | 'CORPORATIVO' | 'PARTICULAR';

export interface Cliente {
  id: number;
  name: string;
  email: string | null;
  whatsapp: string | null;
  location: string | null;
  tier: TipoCliente;
  totalSpent: number;
  reservations: number;
  lastEvent: string | null;
  active: boolean;
  createdAt: string;
}
