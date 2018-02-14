import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

import { ChartsModule } from 'ng2-charts/ng2-charts';

import { VendorModule } from '@syndesis/ui/vendor';
import { SyndesisCommonModule } from '@syndesis/ui/common';
import { IntegrationListModule } from '@syndesis/ui/integration';

import { DashboardComponent } from './dashboard.component';
import { DashboardEmptyComponent } from './dashboard_empty';
import { DashboardConnectionsComponent } from './dashboard_connections';
import { DashboardIntegrationsComponent } from './dashboard_integrations';
import { DashboardMetricsComponent } from './dashboard_metrics';

const routes: Routes = [
  { path: '', component: DashboardComponent, pathMatch: 'full' }
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    SyndesisCommonModule,
    VendorModule,
    ChartsModule,
    IntegrationListModule,
  ],
  declarations: [
    DashboardMetricsComponent,
    DashboardConnectionsComponent,
    DashboardIntegrationsComponent,
    DashboardEmptyComponent,
    DashboardComponent,
  ]
})
export class DashboardModule {}
