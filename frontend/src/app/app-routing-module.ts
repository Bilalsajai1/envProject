import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AuthCallbackComponent } from './auth/auth-callback/auth-callback.component';
import { LoginComponent } from './auth/login/login.component';

import { MainLayout } from './layout/main-layout/main-layout';
import { AuthGuard } from './auth/auth.guard';

import { UserAdminComponent } from './admin/user-admin/user-admin.component';
import { DashboardComponent } from './layout/dashboard/dashboard.component';
import { RoleAdminComponent } from './admin/role-admin/role-admin.component';
import { EnvTypeAdminComponent } from './admin/env-type-admin/env-type-admin.component';

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
      // Home = admin/users (pour l’instant)
      { path: '', redirectTo: 'admin/users', pathMatch: 'full' },

      // --- ADMIN ZONE ---
      { path: 'admin/users', component: UserAdminComponent },
      { path: 'admin/roles', component: RoleAdminComponent },

      // Types d’environnements : /admin/env-types/EDITION, /admin/env-types/INTEGRATION, etc.
      {
        path: 'admin/env-types/:code',   // ⚠️ :code pour matcher EnvTypeAdminComponent
        component: EnvTypeAdminComponent
      },

      // Dashboard (optionnel)
      { path: 'dashboard', component: DashboardComponent },

      // Fallback interne → retourne sur users admin
      { path: '**', redirectTo: 'admin/users' }
    ]
  },

  // Fallback global (si rien ne matche, on renvoie vers login)
  { path: '**', redirectTo: 'login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { scrollPositionRestoration: 'enabled' })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
