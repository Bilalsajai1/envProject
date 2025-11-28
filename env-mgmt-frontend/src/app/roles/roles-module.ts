import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { MaterialModule } from '../shared/material/material.module';
import {RoleListComponent} from './role-list/role-list.component';
import {RoleFormComponent} from './role-form/role-form.component';

@NgModule({
  declarations: [
    RoleListComponent,
    RoleFormComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    RouterModule          // juste pour routerLink, pas de forChild ici
  ],
  exports: [
    RoleListComponent,
    RoleFormComponent
  ]
})
export class RolesModule {}
