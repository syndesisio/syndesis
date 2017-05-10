import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

import { SyndesisCommonModule } from '../common/common.module';

import { ChartsModule } from 'ng2-charts/ng2-charts';
import { ModalModule } from 'ng2-bootstrap';
import { ToasterModule } from 'angular2-toaster';
import { TooltipModule } from 'ng2-bootstrap/tooltip';
import { DropdownModule } from 'ng2-bootstrap/dropdown';

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
    SyndesisCommonModule,
    ChartsModule,
    ModalModule,
    ToasterModule,
    TooltipModule,
    DropdownModule,
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
