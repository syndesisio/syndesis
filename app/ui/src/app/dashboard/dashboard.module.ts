import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

import { ChartsModule } from 'ng2-charts/ng2-charts';

import { SyndesisVendorModule } from '@syndesis/ui/vendor.module';
import { SyndesisCommonModule } from '@syndesis/ui/common';
import { IntegrationListModule } from '@syndesis/ui/integration';

import { DashboardComponent } from './dashboard.component';
import { EmptyStateComponent } from './dashboard_empty';
import { DashboardConnectionsComponent } from './dashboard_connections';
import { DashboardIntegrationsComponent } from './dashboard_integrations';

const routes: Routes = [
  { path: '', component: DashboardComponent, pathMatch: 'full' }
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    SyndesisCommonModule,
    SyndesisVendorModule,
    ChartsModule,
    IntegrationListModule,
  ],
  declarations: [
    DashboardComponent,
    DashboardConnectionsComponent,
    DashboardIntegrationsComponent,
    EmptyStateComponent
  ]
})
export class DashboardModule {}
