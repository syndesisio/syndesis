import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Store, select } from '@ngrx/store';
import { Observable } from 'rxjs';

import {
  PlatformState,
  IntegrationState,
  selectIntegrationState,
  Integrations,
  IntegrationActions,
  Connections,
  Connection
} from '@syndesis/ui/platform';
import { ConnectionStore, IntegrationStore } from '@syndesis/ui/store';

@Component({
  selector: 'syndesis-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  integrationState$: Observable<IntegrationState>;
  connections$: Observable<Connections>;
  integrations$: Observable<Integrations>;
  connectionsLoading$: Observable<boolean>;
  integrationsLoading$: Observable<boolean>;

  constructor(
    private store: Store<PlatformState>,
    private connectionStore: ConnectionStore,
    private integrationStore: IntegrationStore,
    private router: Router
  ) {
    this.connections$ = this.connectionStore.listVisible;
    this.integrations$ = this.integrationStore.list;
    this.connectionsLoading$ = this.connectionStore.loading;
    this.integrationsLoading$ = this.integrationStore.loading;
  }

  ngOnInit() {
    this.integrationState$ = this.store.pipe(select(selectIntegrationState));

    this.connectionStore.loadAll();
    this.integrationStore.loadAll();
  }

  onRefreshDashboard(): void {
    this.store.dispatch(new IntegrationActions.FetchMetrics());
  }

  //-----  Selecting a Connection or Integration ------------------->>
  selectedConnection(connection: Connection) {
    this.router.navigate(['/connections', connection.id]);
  }
}
