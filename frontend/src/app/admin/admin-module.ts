import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AdminRoutingModule } from './admin-routing-module';
import {FormsModule} from '@angular/forms';
import { ProjectAdminComponent } from './project-admin/project-admin.component';
import { ApplicationAdminComponent } from './application-admin/application-admin.component';


@NgModule({
  declarations: [
    ProjectAdminComponent,
    ApplicationAdminComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    AdminRoutingModule
  ]
})
export class AdminModule {}
