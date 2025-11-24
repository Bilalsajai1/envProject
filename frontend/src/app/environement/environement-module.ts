import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { EnvironementRoutingModule } from './environement-routing-module';
import {FormsModule} from '@angular/forms';
import { ApplicationListComponent } from './pages/application-list/application-list.component';
import { EnvironmentListComponent } from './pages/environement-list/environement-list.component';
import { EnvironmentTypeListComponent } from './pages/environement-type-list/environement-type-list.component';
import { ProjectListComponent } from './pages/project-list/project-list.component';


@NgModule({
  declarations: [


    ApplicationListComponent,
    EnvironmentListComponent,
    EnvironmentTypeListComponent,
    ProjectListComponent
  ],
  imports: [
    CommonModule,
    EnvironementRoutingModule ,
    FormsModule
  ]
})
export class EnvironementModule { }
