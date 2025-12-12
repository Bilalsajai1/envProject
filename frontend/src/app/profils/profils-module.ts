import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MaterialModule } from '../shared/material/material.module';
import {ProfilListComponent} from './components/profil-list/profil-list.component/profil-list.component';
import {ProfilFormComponent} from './components/profil-form/profil-form.component/profil-form.component';

@NgModule({
  declarations: [
    ProfilListComponent,
    ProfilFormComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    RouterModule
  ],
  exports: [
    ProfilListComponent,
    ProfilFormComponent
  ]
})
export class ProfilsModule {}
