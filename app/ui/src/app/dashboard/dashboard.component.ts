import { Component, OnInit, OnDestroy } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs/Observable';

import { ConfigService } from '@syndesis/ui/config.service';
import {
  PlatformState,
  IntegrationState, selectIntegrationState, Integrations, IntegrationActions,
  Connections,
  UserService
} from '@syndesis/ui/platform';
import { ConnectionStore, IntegrationStore } from '@syndesis/ui/store';

const DEFAULT_POLLING_INTERVAL = 5000;

@Component({
  selector: 'syndesis-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit, OnDestroy {
  integrationState$: Observable<IntegrationState>;
  connections$: Observable<Connections>;
  integrations$: Observable<Integrations>;
  connectionsLoading$: Observable<boolean>;
  integrationsLoading$: Observable<boolean>;

  private metricsRefreshInterval: any;

  constructor(
    private store: Store<PlatformState>,
    private userService: UserService,
    private connectionStore: ConnectionStore,
    private integrationStore: IntegrationStore,
    private configService: ConfigService
  ) {
    this.connections$ = this.connectionStore.list;
    this.integrations$ = this.integrationStore.list;
    this.connectionsLoading$ = this.connectionStore.loading;
    this.integrationsLoading$ = this.integrationStore.loading;
  }

  ngOnInit() {
    this.integrationState$ = this.store.select(selectIntegrationState);

    this.connectionStore.loadAll();
    this.integrationStore.loadAll();

    let pollingInterval: number;

    try {
      pollingInterval = this.configService.getSettings('metricsPollingInterval');
    } catch (error) {
      pollingInterval = DEFAULT_POLLING_INTERVAL;
    }

    if (pollingInterval && !isNaN(pollingInterval) && pollingInterval > 0) {
      this.metricsRefreshInterval = setInterval(() => this.refreshDashboard(), pollingInterval);
    }
  }

  ngOnDestroy() {
    if (this.metricsRefreshInterval) {
      clearInterval(this.metricsRefreshInterval);
    }
  }

  private refreshDashboard(): void {
    this.store.dispatch(new IntegrationActions.FetchMetrics());
  }
}
