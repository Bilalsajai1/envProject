import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {EnvironmentTypeListComponent} from './pages/environement-type-list/environement-type-list.component';
import {ProjectListComponent} from './pages/project-list/project-list.component';
import {EnvironmentListComponent} from './pages/environement-list/environement-list.component';
import {ApplicationListComponent} from './pages/application-list/application-list.component';

const routes: Routes = [
  { path: '', component: EnvironmentTypeListComponent },
  { path: ':typeCode/projects', component: ProjectListComponent },
  { path: ':typeCode/projects/:projectId/environments', component: EnvironmentListComponent },
  { path: ':typeCode/projects/:projectId/environments/:envId/applications', component: ApplicationListComponent }

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EnvironementRoutingModule { }
