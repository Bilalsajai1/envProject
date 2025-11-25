import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {ProjectAdminComponent} from './project-admin/project-admin.component';
import {ApplicationAdminComponent} from './application-admin/application-admin.component';
import {MenuAdminComponent} from './menu-admin/menu-admin.component';
import {ProfileAdminComponent} from './profile-admin/profile-admin.component';

const routes: Routes = [
  {
    path: 'projects',
    component: ProjectAdminComponent
  },
  {
    path: 'applications',
    component: ApplicationAdminComponent
  },
  {
    path: 'menus',
    component: MenuAdminComponent
  },
  {
    path: 'profiles',
    component: ProfileAdminComponent
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'projects'
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule {}
