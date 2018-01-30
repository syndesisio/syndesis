import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { Connections, Integrations } from '../model';
import { ConnectionStore } from '../store/connection/connection.store';
import { IntegrationStore } from '../store/integration/integration.store';
import { TourService } from 'ngx-tour-ngx-bootstrap';
import { UserService } from '../common/user.service';

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
    public tourService: TourService,
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

    /**
     * If guided tour state is set to be shown (i.e. true), then show it for this page, otherwise don't.
     */
    /*
    if (this.userService.getTourState() === true) {
      this.tourService.initialize([ {
        route: 'dashboard',
        anchorId: 'dashboard.navigation',
        title: 'Navigation',
        content: 'View integrations, connections or settings for applications that Fuse Ignite is registered with.',
        placement: 'right',
        }, {
        route: 'dashboard',
        anchorId: 'dashboard.integration',
        title: 'Create Integration',
        placement: 'bottom',
        content: 'After creating at least two connections, you can create an integration.',
        } ], {
          route: 'dashboard',
        }
      );
      setTimeout(() => this.tourService.start());
    }
    */
  }
}
