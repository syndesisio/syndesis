import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

import { DonutChartModule as ChartModule } from 'patternfly-ng';

import { VendorModule } from '@syndesis/ui/vendor';
import { SyndesisCommonModule } from '@syndesis/ui/common';
import { IntegrationListModule } from '@syndesis/ui/integration';

import { DashboardComponent } from '@syndesis/ui/dashboard/dashboard.component';
import { DashboardConnectionsComponent } from '@syndesis/ui/dashboard/dashboard_connections';
import { DashboardIntegrationsComponent } from '@syndesis/ui/dashboard/dashboard_integrations';
import { DashboardMetricsComponent } from '@syndesis/ui/dashboard/dashboard_metrics';

const routes: Routes = [
  { path: '', component: DashboardComponent, pathMatch: 'full' }
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    SyndesisCommonModule,
    VendorModule,
    ChartModule,
    IntegrationListModule
  ],
  declarations: [
    DashboardMetricsComponent,
    DashboardConnectionsComponent,
    DashboardIntegrationsComponent,
    DashboardComponent
  ]
})
export class DashboardModule {}
