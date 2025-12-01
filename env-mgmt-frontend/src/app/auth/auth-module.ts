// src/app/auth/auth-module.ts

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from '../shared/material/material.module';

import { LoginComponent } from './login/login.component';
import {ForgotPasswordComponent} from './forgot-password/forgot-password.component/forgot-password.component';
import {ResetPasswordComponent} from './reset-password/reset-password.component/reset-password.component';
import {AuthRoutingModule} from './auth-routing-module';


@NgModule({
  declarations: [
    LoginComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    AuthRoutingModule,
    MaterialModule
  ]
})
export class AuthModule {}
