import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MainLayout } from './main-layout/main-layout';
import { DashboardComponent } from './dashboard/dashboard.component';
import { SidebarComponent } from './sidebar/sidebar.component';
import { TopbarComponent } from './topbar/topbar.component';
import { TranslateModule } from '@ngx-translate/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NgbDropdown, NgbDropdownMenu, NgbDropdownToggle } from '@ng-bootstrap/ng-bootstrap';
import { NgxSpinnerComponent } from 'ngx-spinner';
import { EnvironementModule } from '../environement/environement-module';
import { DataTableComponent } from './data-table/data-table.component';

// Material pour la datatable
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@NgModule({
  declarations: [
    MainLayout,
    DashboardComponent,
    SidebarComponent,
    TopbarComponent,
    DataTableComponent
  ],
  exports: [
    MainLayout,
    DashboardComponent,
    DataTableComponent
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
    EnvironementModule,

    // Material table
    MatFormFieldModule,
    MatInputModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatIconModule,
    MatButtonModule
  ]
})
export class LayoutModule { }
