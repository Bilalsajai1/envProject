import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AuthCallbackComponent } from './auth/auth-callback/auth-callback.component';
import { LoginComponent } from './auth/login/login.component';

import { MainLayout } from './layout/main-layout/main-layout';
import { AuthGuard } from './auth/auth.guard';

import { UserAdminComponent } from './admin/user-admin/user-admin.component';
import { DashboardComponent } from './layout/dashboard/dashboard.component';
import {RoleAdminComponent} from './admin/role-admin/role-admin.component';
import {EnvTypeAdminComponent} from './admin/env-type-admin/env-type-admin.component';

const routes: Routes = [
  // 1) Login & callback (PAS de guard)
  { path: 'login', component: LoginComponent },
  { path: 'auth/callback', component: AuthCallbackComponent },

  // 2) Tout le reste derrière MainLayout + AuthGuard
  {
    path: '',
    component: MainLayout,
    canActivate: [AuthGuard],
    children: [
      // Home = admin/users (car tu as un seul user admin pour le moment)
      { path: '', redirectTo: 'admin/users', pathMatch: 'full' },

      // --- ADMIN ZONE ---
      { path: 'admin/users', component: UserAdminComponent },
      { path: 'admin/roles', component: RoleAdminComponent },

      // 3 menus pour les types d’environnements
      // Exemple : EDITION, INTEGRATION, CLIENT
      {
        path: 'admin/env-types/:typeCode',
        component: EnvTypeAdminComponent
      },

      // Dashboard (non utilisé comme home pour l’instant, mais dispo)
      { path: 'dashboard', component: DashboardComponent },

      // Fallback interne
      { path: '**', redirectTo: 'admin/users' }
    ]
  },

  // Fallback global
  { path: '**', redirectTo: 'login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { scrollPositionRestoration: 'enabled' })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
