import { Routes } from '@angular/router';
import { Home } from './features/home/home';
import { Catalog } from './features/catalog/catalog';
import { Services } from './features/services/services';
import { Contact } from './features/contact/contact';
import { Login } from './features/login/login';
import { CatalogAdmin } from './features/admin/catalog-admin';
import { Placeholder } from './features/placeholder/placeholder';
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
  { path: 'cotizador', component: Placeholder, data: { titulo: 'Cotizador' } },
  { path: '**', redirectTo: '' },
];
