// src/app/environments/environments.module.ts

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { MaterialModule } from '../shared/material/material.module';

// Components


// Dialogs
import { ProjectDialogComponent } from './components/dialogs/project-dialog/project-dialog.component';
import { EnvironmentDialogComponent } from './components/dialogs/environment-dialog/environment-dialog.component';
import { ApplicationDialogComponent } from './components/dialogs/application-dialog/application-dialog.component';
import {EnvironmentTypeLayoutComponent} from './environment-type-layout/environment-type-layout.component';
import {ProjectListComponent} from './project-list/project-list.component';
import {EnvironmentListComponent} from './environment-list/environment-list.component';
import {ApplicationListComponent} from './application-list/application-list.component';

@NgModule({
  declarations: [
    EnvironmentTypeLayoutComponent,
    ProjectListComponent,
    EnvironmentListComponent,
    ApplicationListComponent,
    ProjectDialogComponent,
    EnvironmentDialogComponent,
    ApplicationDialogComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    MaterialModule
  ]
})
export class EnvironmentsModule {}
