import { Component } from '@angular/core';

@Component({
  selector: 'syndesis-monitor',
  template: `
  <ul class="syn-tabs row nav nav-tabs nav-tabs-pf toolbar-pf">
    <li class="syn-tabs__tab" routerLinkActive="active">
      <a [routerLink]="['metrics']">Metrics</a>
    </li>
    <li class="syn-tabs__tab" routerLinkActive="active">
      <a [routerLink]="['logs']">Logs</a>
    </li>
  </ul>

  <router-outlet></router-outlet>
  `
})
export class MonitorComponent {}
