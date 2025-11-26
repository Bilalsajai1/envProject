import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AdminRoutingModule } from './admin-routing-module';
import { FormsModule } from '@angular/forms';
import { ProjectAdminComponent } from './project-admin/project-admin.component';
import { ApplicationAdminComponent } from './application-admin/application-admin.component';
import { MenuAdminComponent } from './menu-admin/menu-admin.component';
import { ProfileAdminComponent } from './profile-admin/profile-admin.component';
import { UserAdminComponent } from './user-admin/user-admin.component';
import { ProfilRoleComponent } from './profil-role/profil-role.component';
import { RoleAdminComponent } from './role-admin/role-admin.component';
import { EnvTypeAdminComponent } from './env-type-admin/env-type-admin.component';
import {MatIcon} from "@angular/material/icon-module.d";
import {MatPaginator} from '@angular/material/paginator.d';
import {MatButton, MatIconButton} from '@angular/material/button';

@NgModule({
  declarations: [
    ProjectAdminComponent,
    ApplicationAdminComponent,
    MenuAdminComponent,
    ProfileAdminComponent,
    UserAdminComponent,
    ProfilRoleComponent,
    RoleAdminComponent,
    EnvTypeAdminComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    AdminRoutingModule,
    MatIcon,
    MatPaginator,
    MatButton,
    MatIconButton
  ]
})
export class AdminModule {}
