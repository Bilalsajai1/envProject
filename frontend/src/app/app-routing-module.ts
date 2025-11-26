import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './layout/dashboard/dashboard.component';
import { MainLayout } from './layout/main-layout/main-layout';
import { AuthGuard } from './auth/auth.guard';
import { RoleGuard } from './auth/role.guard';  // ✅ IMPORT
import { AuthCallbackComponent } from './auth/auth-callback/auth-callback.component';

const routes: Routes = [
  {
    path: 'auth/callback',
    component: AuthCallbackComponent
  },

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
        canActivate: [RoleGuard],  // ✅ AJOUTER LE GUARD
        data: { roles: ['ADMIN'] }, // ✅ SPÉCIFIER LES RÔLES REQUIS
        loadChildren: () =>
          import('./admin/admin-module').then(m => m.AdminModule)
      },

      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },

  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
