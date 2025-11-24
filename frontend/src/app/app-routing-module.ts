import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {DashboardComponent} from './layout/dashboard/dashboard.component';
import {MainLayout} from './layout/main-layout/main-layout';

const routes: Routes = [
  {
    path: '',
    component: MainLayout,
    children: [
      { path: 'dashboard', component: DashboardComponent },
      {
        path: 'environment',
        loadChildren: () =>
          import('./environement/environement-module').then(m => m.EnvironementModule)
      },
      {
        path: 'admin',
        loadChildren: () =>
          import('./admin/admin-module').then(m => m.AdminModule)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
