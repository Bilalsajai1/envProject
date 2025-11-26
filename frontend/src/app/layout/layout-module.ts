import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MainLayout } from './main-layout/main-layout';
import { DashboardComponent } from './dashboard/dashboard.component';
import { SidebarComponent } from './sidebar/sidebar.component';
import { TopbarComponent } from './topbar/topbar.component';
import {TranslateModule, TranslatePipe} from '@ngx-translate/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {NgbDropdown, NgbDropdownMenu, NgbDropdownToggle} from '@ng-bootstrap/ng-bootstrap';
import {NgxSpinnerComponent} from 'ngx-spinner';
import {EnvironementModule} from '../environement/environement-module';



@NgModule({
  declarations: [
    MainLayout,
    DashboardComponent,
    SidebarComponent,
    TopbarComponent
  ],
  exports: [
    MainLayout,
    DashboardComponent
  ],
  imports: [
    CommonModule,
    NgbDropdownToggle,
    NgbDropdownMenu,
    NgbDropdown,
    TranslateModule.forChild(),
    NgxSpinnerComponent,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    EnvironementModule
  ]
})
export class LayoutModule { }
