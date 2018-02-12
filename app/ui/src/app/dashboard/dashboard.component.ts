import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { Connections, Integrations, UserService } from '@syndesis/ui/platform';
import { ConnectionStore, IntegrationStore } from '@syndesis/ui/store';

@Component({
  selector: 'syndesis-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  connections: Observable<Connections>;
  integrations: Observable<Integrations>;
  connectionsLoading: Observable<boolean>;
  integrationsLoading: Observable<boolean>;
  selectedId = undefined;
  truncateLimit = 80;
  truncateTrail = '…';

  constructor(
    private connectionStore: ConnectionStore,
    private integrationStore: IntegrationStore,
    private userService: UserService
  ) {
    this.connections = this.connectionStore.list;
    this.integrations = this.integrationStore.list;
    this.connectionsLoading = this.connectionStore.loading;
    this.integrationsLoading = this.integrationStore.loading;
  }

  ngOnInit() {
    this.connectionStore.loadAll();
    this.integrationStore.loadAll();
  }
}
