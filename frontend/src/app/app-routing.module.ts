
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { AuthGuard } from './auth/guards/auth-guard';

// === Administration ===
import { UserListComponent } from './users/components/user-list/user-list.component';
import { UserFormComponent } from './users/components/user-form/user-form.component';

import { PermissionManagementComponent } from './permissions/permission-management/permission-management.component';

import {ProfilListComponent} from './profils/components/profil-list/profil-list.component/profil-list.component';
import {ProfilFormComponent} from './profils/components/profil-form/profil-form.component/profil-form.component';

// === Environnements ===
import { EnvironmentTypeLayoutComponent } from './environments/environment-type-layout/environment-type-layout.component';
import { ProjectListComponent } from './environments/project-list/project-list.component';
import { EnvironmentListComponent } from './environments/environment-list/environment-list.component';
import { ApplicationListComponent } from './environments/application-list/application-list.component';


const routes: Routes = [
  {
    path: '',
    redirectTo: 'auth/login',
    pathMatch: 'full'
  },

  // Auth (lazy)
  {
    path: 'auth',
    loadChildren: () =>
      import('./auth/auth-module').then(m => m.AuthModule)
  },

  // Layout principal protégé
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      // ==========================
      // ADMINISTRATION
      // ==========================
      {
        path: 'admin',
        children: [
          // --- Profils ---
          {
            path: 'profils',
            component: ProfilListComponent,
            canActivate: [AuthGuard],
            data: { roles: ['ADMIN'] }
          },
          {
            path: 'profils/new',
            component: ProfilFormComponent,
            canActivate: [AuthGuard],
            data: { roles: ['ADMIN'] }
          },
          {
            path: 'profils/:id/edit',
            component: ProfilFormComponent,
            canActivate: [AuthGuard],
            data: { roles: ['ADMIN'] }
          },

          // --- Utilisateurs ---
          {
            path: 'users',
            component: UserListComponent,
            canActivate: [AuthGuard],
            data: { roles: ['ADMIN', 'ROLE_USERS_ACCESS'] }
          },
          {
            path: 'users/new',
            component: UserFormComponent,
            canActivate: [AuthGuard],
            data: { roles: ['ADMIN', 'ROLE_USERS_CREATE'] }
          },
          {
            path: 'users/:id/edit',
            component: UserFormComponent,
            canActivate: [AuthGuard],
            data: { roles: ['ADMIN', 'ROLE_USERS_EDIT'] }
          },

          // --- Permissions ---
          {
            path: 'permissions',
            component: PermissionManagementComponent,
            canActivate: [AuthGuard],
            data: { roles: ['ADMIN', 'ROLE_ROLES_EDIT'] }
          },
        ]
      },

      // ==========================
      // ENVIRONNEMENTS
      // ==========================
      {
        path: 'env/:typeCode',
        component: EnvironmentTypeLayoutComponent,
        canActivate: [AuthGuard],
        children: [
          // Liste des projets
          {
            path: '',
            component: ProjectListComponent
          },
          // Liste des environnements d'un projet
          {
            path: ':projectId/environments',
            component: EnvironmentListComponent
          },
          // Liste des applications d'un environnement
          {
            path: ':projectId/environments/:environmentId/applications',
            component: ApplicationListComponent
          }
        ]
      }
    ]
  },

  // Fallback
  { path: '**', redirectTo: 'auth/login' }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      scrollPositionRestoration: 'top',
      onSameUrlNavigation: 'reload'
    })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {}
