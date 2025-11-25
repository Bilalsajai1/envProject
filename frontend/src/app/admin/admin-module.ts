import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AdminRoutingModule } from './admin-routing-module';
import {FormsModule} from '@angular/forms';
import { ProjectAdminComponent } from './project-admin/project-admin.component';
import { ApplicationAdminComponent } from './application-admin/application-admin.component';
import { MenuAdminComponent } from './menu-admin/menu-admin.component';
import { ProfileAdminComponent } from './profile-admin/profile-admin.component';


@NgModule({
  declarations: [
    ProjectAdminComponent,
    ApplicationAdminComponent,
    MenuAdminComponent,
    ProfileAdminComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    AdminRoutingModule
  ]
})
export class AdminModule {}
