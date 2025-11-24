import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {ProjectAdminComponent} from './project-admin/project-admin.component';
import {ApplicationAdminComponent} from './application-admin/application-admin.component';

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
