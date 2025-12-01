// src/app/app.module.ts

import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MaterialModule } from './shared/material/material.module';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { AuthInterceptor } from './auth/interceptors/auth.interceptor';

// Modules
import { UsersModule } from './users/users-module';
import { RolesModule } from './roles/roles-module';
import {PermissionsModule} from './permissions/permissions-module';
import {EnvironmentsModule} from './environments/environments-module';
import {ProfilsModule} from './profils/profils-module';


@NgModule({
  declarations: [
    AppComponent,
    MainLayoutComponent
  ],
  imports: [
    ProfilsModule,
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    UsersModule,
    RolesModule,
    PermissionsModule,
    EnvironmentsModule  // ✅ NOUVEAU MODULE
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
