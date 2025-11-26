import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { TranslateModule, TranslatePipe } from '@ngx-translate/core';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { NgxSpinnerModule } from 'ngx-spinner';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { LayoutModule } from './layout/layout-module';
import { AuthCallbackComponent } from './auth/auth-callback/auth-callback.component';
import { AuthInterceptor } from './auth/auth.interceptor';
import { LoginComponent } from './auth/login/login.component';
import { UsersComponent } from './user/users/users.component';
import { UserFormComponent } from './user/user-form/user-form.component';
import { ReactiveFormsModule } from '@angular/forms';

// Angular Material
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@NgModule({
  declarations: [
    App,
    AuthCallbackComponent,
    LoginComponent,
    UsersComponent,
    UserFormComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    TranslateModule.forRoot(),
    AppRoutingModule,
    NgbModule,
    NgxSpinnerModule,
    ToastrModule.forRoot({
      timeOut: 5000,
      positionClass: 'toast-top-right',
      progressBar: true,
      progressAnimation: 'increasing',
      preventDuplicates: true,
    }),
    LayoutModule,
    ReactiveFormsModule,

    // Material
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatIconModule,
    MatButtonModule,
  ],
  providers: [
    provideHttpClient(withInterceptorsFromDi()),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    TranslatePipe,
    ToastrService,
  ],
  bootstrap: [App]
})
export class AppModule {}
