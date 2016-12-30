import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DashboardComponent } from './dashboard.component';
import { EmptyStateComponent } from './emptystate.component';

const routes: Routes = [
  { path: '', component: DashboardComponent, pathMatch: 'full' }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes)
  ],
  declarations: [
    DashboardComponent,
    EmptyStateComponent
  ]
})
export class DashboardModule { }
