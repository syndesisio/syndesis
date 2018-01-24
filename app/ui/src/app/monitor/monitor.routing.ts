import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { MonitorComponent } from './monitor.component';
import { SystemMetricsComponent } from './system-metrics';
import { SystemLogsComponent } from './system-logs';

const routes: Routes = [{
  path: '',
  component: MonitorComponent,
  children: [{
    path: '',
    redirectTo: 'metrics',
    pathMatch: 'full'
  }, {
    path: 'metrics',
    component: SystemMetricsComponent
  }, {
    path: 'logs',
    component: SystemLogsComponent
  },
  ]
}];

export const routedComponents = [
  MonitorComponent,
  SystemMetricsComponent, SystemLogsComponent,
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  declarations: [...routedComponents]
})
export class MonitorRoutingModule { }
