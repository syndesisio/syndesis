import { Component, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs/Observable';

import {
  PlatformState,
  IntegrationState, selectIntegrationState, Integrations, IntegrationActions,
  Connections,
  UserService
} from '@syndesis/ui/platform';
import { ConnectionStore, IntegrationStore } from '@syndesis/ui/store';

@Component({
  selector: 'syndesis-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  integrationState$: Observable<IntegrationState>;
  connections$: Observable<Connections>;
  integrations: Observable<Integrations>;
  connectionsLoading: Observable<boolean>;
  integrationsLoading: Observable<boolean>;
  selectedId = undefined;
  truncateLimit = 80;
  truncateTrail = '…';

  constructor(
    private store: Store<PlatformState>,
    private userService: UserService,
    private connectionStore: ConnectionStore,
    private integrationStore: IntegrationStore,
  ) {
    this.connections$ = this.connectionStore.list;
    this.integrations = this.integrationStore.list;
    this.connectionsLoading = this.connectionStore.loading;
    this.integrationsLoading = this.integrationStore.loading;
  }

  ngOnInit() {
    this.integrationState$ = this.store.select(selectIntegrationState);

    this.connectionStore.loadAll();
    this.integrationStore.loadAll();
  }

  onMetricsRefresh(): void {
    this.store.dispatch(new IntegrationActions.FetchMetrics());
  }
}
