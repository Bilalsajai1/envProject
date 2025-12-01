// src/app/app.module.ts

import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MaterialModule } from './shared/material/material.module';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';

import { UsersModule } from './users/users-module';
import { RolesModule } from './roles/roles-module';
import { PermissionsModule } from './permissions/permissions-module';
import { EnvironmentsModule } from './environments/environments-module';
import { ProfilsModule } from './profils/profils-module';

import { AuthInterceptor } from './auth/interceptors/auth.interceptor';

@NgModule({
  declarations: [
    AppComponent,
    MainLayoutComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,

    // Feature modules
    UsersModule,
    RolesModule,
    PermissionsModule,
    EnvironmentsModule,
    ProfilsModule,

    // Routing en dernier
    AppRoutingModule
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
