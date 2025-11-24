import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './layout/dashboard/dashboard.component';
import { MainLayout } from './layout/main-layout/main-layout';
import { AuthGuard } from './auth/auth.guard';
import {AuthCallbackComponent} from './auth/auth-callback/auth-callback.component';

const routes: Routes = [

  // === CALLBACK KEYCLOAK (PAS DE GUARD) ===
  {
    path: 'auth/callback',
    component: AuthCallbackComponent
  },

  // === ROUTES PROTÉGÉES ===
  {
    path: '',
    component: MainLayout,
    canActivate: [AuthGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },

      {
        path: 'environment',
        loadChildren: () =>
          import('./environement/environement-module').then(m => m.EnvironementModule)
      },

      {
        path: 'admin',
        loadChildren: () =>
          import('./admin/admin-module').then(m => m.AdminModule)
      },

      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },

  // === CATCH-ALL ===
  {
    path: '**',
    redirectTo: ''
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
