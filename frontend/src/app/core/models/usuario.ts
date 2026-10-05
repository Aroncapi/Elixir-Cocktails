export interface Usuario {
  id: number;
  email: string;
  fullName: string;
  role: 'ADMIN' | 'STAFF';
  active: boolean;
}
