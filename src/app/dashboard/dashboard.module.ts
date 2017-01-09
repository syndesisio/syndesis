import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';

import {DashboardComponent} from './dashboard.component';
import {EmptyStateComponent} from './emptystate.component';
import {PopularTemplatesComponent} from './populartemplates.component';
import {SharedModule} from '../shared/shared.module';

const routes: Routes = [
  {path: '', component: DashboardComponent, pathMatch: 'full'},
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    SharedModule,
  ],
  declarations: [
    DashboardComponent,
    EmptyStateComponent,
    PopularTemplatesComponent,
  ],
})
export class DashboardModule {
}
