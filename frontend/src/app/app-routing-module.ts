import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './layout/dashboard/dashboard.component';
import { MainLayout } from './layout/main-layout/main-layout';
import { AuthGuard } from './auth/auth.guard';
import {AuthCallbackComponent} from './auth/auth-callback/auth-callback.component';

const routes: Routes = [
  // Callback Keycloak : PAS de guard ici
  {
    path: 'auth/callback',
    component: AuthCallbackComponent
  },

  // Le reste de l’appli est protégé
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

  // fallback
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
