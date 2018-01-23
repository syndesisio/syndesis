import { Component, Input, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Router } from '@angular/router';

import { log, getCategory } from '../logging';

import { Connection, Connections, Integrations } from '../model';
import { ConnectionStore } from '../store/connection/connection.store';
import { TourService } from 'ngx-tour-ngx-bootstrap';
import { UserService } from '@syndesis/ui/platform';

const category = getCategory('Dashboard');

@Component({
  selector: 'syndesis-dashboard-empty-state',
  templateUrl: './emptystate.component.html',
  styleUrls: ['./emptystate.component.scss']
})
export class EmptyStateComponent implements OnInit {
  connections: Observable<Connections>;
  @Input() loading: boolean;
  @Input() integrations: Integrations;
  truncateLimit = 80;
  truncateTrail = '…';

  constructor(
    private connectionStore: ConnectionStore,
    private router: Router,
    public tourService: TourService,
    private userService: UserService
  ) {
    this.connections = this.connectionStore.list;
  }

  selectedConnection(connection: Connection) {
    this.router.navigate(['/connections', connection.id]);
  }

  ngOnInit() {
    this.connectionStore.loadAll();
    /**
     * If guided tour state is set to be shown (i.e. true), then show it for this page, otherwise don't.
     */
    if (this.userService.getTourState() === true) {
      this.tourService.initialize([
        {
          anchorId: 'dashboard.integration',
          content: 'Create Integration',
          placement: 'bottom',
          title:
            'After creating at least two connections, you can create an integration.'
        }
      ]);
      setTimeout(() => this.tourService.start());
    }
  }
}
