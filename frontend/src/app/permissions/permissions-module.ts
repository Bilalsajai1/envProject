import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MaterialModule } from '../shared/material/material.module';
import { PermissionManagementComponent } from './permission-management/permission-management.component';
import { MatStepperModule } from '@angular/material/stepper';

@NgModule({
  declarations: [
    PermissionManagementComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    RouterModule,
    MatStepperModule
  ],
  exports: [
    PermissionManagementComponent
  ]
})
export class PermissionsModule {}
