// src/app/app-routing.module.ts

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { AuthGuard } from './auth/guards/auth-guard';

// Users
import { UserListComponent } from './users/components/user-list/user-list.component';
import { UserFormComponent } from './users/components/user-form/user-form.component';



// Roles
import { RoleListComponent } from './roles/role-list/role-list.component';
import { RoleFormComponent } from './roles/role-form/role-form.component';
import {PermissionManagementComponent} from './permissions/permission-management/permission-management.component';
import {EnvironmentTypeLayoutComponent} from './environments/environment-type-layout/environment-type-layout.component';
import {ProjectListComponent} from './environments/project-list/project-list.component';
import {EnvironmentListComponent} from './environments/environment-list/environment-list.component';
import {ApplicationListComponent} from './environments/application-list/application-list.component';



const routes: Routes = [
  {
    path: '',
    redirectTo: 'auth/login',
    pathMatch: 'full'
  },

  {
    path: 'auth',
    loadChildren: () =>
      import('./auth/auth-module').then(m => m.AuthModule)
  },

  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [AuthGuard],
    children: [

      // ========== ADMINISTRATION ==========

      // Utilisateurs
      {
        path: 'admin/users',
        component: UserListComponent,
        canActivate: [AuthGuard],
        data: { roles: ['ADMIN', 'ROLE_USERS_ACCESS'] }
      },
      {
        path: 'admin/users/new',
        component: UserFormComponent,
        canActivate: [AuthGuard],
        data: { roles: ['ADMIN', 'ROLE_USERS_CREATE'] }
      },
      {
        path: 'admin/users/:id/edit',
        component: UserFormComponent,
        canActivate: [AuthGuard],
        data: { roles: ['ADMIN', 'ROLE_USERS_EDIT'] }
      },

      // Permissions
      {
        path: 'admin/permissions',
        component: PermissionManagementComponent,
        canActivate: [AuthGuard],
        data: { roles: ['ADMIN', 'ROLE_ROLES_EDIT'] }
      },

      // Rôles
      {
        path: 'admin/roles',
        component: RoleListComponent,
        canActivate: [AuthGuard],
        data: { roles: ['ADMIN', 'ROLE_ROLES_ACCESS'] }
      },
      {
        path: 'admin/roles/new',
        component: RoleFormComponent,
        canActivate: [AuthGuard],
        data: { roles: ['ADMIN', 'ROLE_ROLES_CREATE'] }
      },
      {
        path: 'admin/roles/:id/edit',
        component: RoleFormComponent,
        canActivate: [AuthGuard],
        data: { roles: ['ADMIN', 'ROLE_ROLES_EDIT'] }
      },
      // ========== ENVIRONNEMENTS ==========

      {
        path: 'env/:typeCode',
        component: EnvironmentTypeLayoutComponent,
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

  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
