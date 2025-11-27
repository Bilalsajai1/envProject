import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { UserListComponent } from './users/components/user-list/user-list.component';
import { UserFormComponent } from './users/components/user-form/user-form.component';
import { AuthGuard } from './auth/guards/auth-guard';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'admin/users',
    pathMatch: 'full'
  },

  // Module d'auth (lazy)
  {
    path: 'auth',
    loadChildren: () => import('./auth/auth-module').then(m => m.AuthModule)
  },

  // Zone sécurisée
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: 'admin/users', component: UserListComponent, data: { roles: ['ROLE_USERS_ACCESS'] } },
      { path: 'admin/users/new', component: UserFormComponent, data: { roles: ['ROLE_USERS_CREATE'] } },
      { path: 'admin/users/:id/edit', component: UserFormComponent, data: { roles: ['ROLE_USERS_EDIT'] } }
    ]
  },

  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
