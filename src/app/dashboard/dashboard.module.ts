import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

import { IPaaSCommonModule } from '../common/common.module';

import { ModalModule } from 'ng2-bootstrap';
import { ToasterModule } from 'angular2-toaster';

import { DashboardComponent } from './dashboard.component';
import { EmptyStateComponent } from './emptystate.component';

import { PopularTemplatesComponent } from './populartemplates.component';
import { TemplatesModule } from '../templates/templates.module';

import { DashboardConnectionsComponent } from './connections.component';
import { DashboardIntegrationsComponent } from './integrations.component';

const routes: Routes = [
  { path: '', component: DashboardComponent, pathMatch: 'full' },
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    TemplatesModule,
    IPaaSCommonModule,
    ToasterModule,
    ModalModule,
  ],
  declarations: [
    DashboardComponent,
    DashboardConnectionsComponent,
    DashboardIntegrationsComponent,
    EmptyStateComponent,
    PopularTemplatesComponent,
  ],
})
export class DashboardModule {
}
