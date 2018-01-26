import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Store } from '@ngrx/store';

import { MonitorStore, MonitorState, getMonitorState } from '@syndesis/ui/monitor';

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
export class MonitorComponent implements OnInit {
  monitorState$: Observable<MonitorState>;

  constructor(private monitorStore: Store<MonitorStore>) { }

  ngOnInit() {
    this.monitorState$ = this.monitorStore.select<MonitorState>(getMonitorState);
  }
}
