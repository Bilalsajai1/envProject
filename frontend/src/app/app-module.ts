import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import {TranslateModule, TranslatePipe} from '@ngx-translate/core';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {NgxSpinnerModule} from 'ngx-spinner';
import {ToastrModule, ToastrService} from 'ngx-toastr';
import {provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {provideTranslateHttpLoader} from '@ngx-translate/http-loader';
import {LayoutModule} from './layout/layout-module';

@NgModule({
  declarations: [
    App
  ],
  imports: [
    BrowserModule,
    TranslateModule.forRoot(),
    AppRoutingModule,
    NgbModule,
    NgxSpinnerModule,
    TranslateModule.forRoot(),
    ToastrModule.forRoot({
      timeOut: 5000,
      positionClass: 'toast-top-right',
      progressBar: true,
      progressAnimation: 'increasing',
      preventDuplicates: true,
    }),
    LayoutModule,
  ],
  providers: [
    provideHttpClient(withInterceptorsFromDi()),
    provideTranslateHttpLoader({
      prefix: './assets/i18n/',
      suffix: '.json'
    }),
    TranslatePipe,
    ToastrService,

  ],
  exports: [],
  bootstrap: [App]
})
export class AppModule { }
