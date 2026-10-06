import { Routes } from '@angular/router';
import { Home } from './features/home/home';
import { Catalog } from './features/catalog/catalog';
import { Services } from './features/services/services';
import { Contact } from './features/contact/contact';
import { Login } from './features/login/login';
import { CatalogAdmin } from './features/admin/catalog-admin';
import { ClientsAdmin } from './features/admin/clients-admin';
import { Quote } from './features/quote/quote';
import { RequestsAdmin } from './features/admin/requests-admin';
import { RequestDetail } from './features/admin/request-detail';
import { Calendario } from './features/admin/calendario';
import { Configuracion } from './features/admin/configuracion';
import { Seguimiento } from './features/seguimiento/seguimiento';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'catalogo', component: Catalog },
  { path: 'servicios', component: Services },
  { path: 'contacto', component: Contact },
  { path: 'login', component: Login },
  {
    path: 'panel',
    component: CatalogAdmin,
    canActivate: [authGuard],
  },
  {
    path: 'panel/clientes',
    component: ClientsAdmin,
    canActivate: [authGuard],
  },
  {
    path: 'panel/solicitudes',
    component: RequestsAdmin,
    canActivate: [authGuard],
  },
  {
    path: 'panel/dashboard',
    component: RequestsAdmin,
    canActivate: [authGuard],
  },
  {
    path: 'panel/calendario',
    component: Calendario,
    canActivate: [authGuard],
  },
  {
    path: 'panel/solicitudes/:id',
    component: RequestDetail,
    canActivate: [authGuard],
  },
  {
    path: 'panel/configuracion',
    component: Configuracion,
    canActivate: [authGuard],
  },
  { path: 'cotizador', component: Quote },
  { path: 'solicitud/:token', component: Seguimiento },
  { path: '**', redirectTo: '' },
];
