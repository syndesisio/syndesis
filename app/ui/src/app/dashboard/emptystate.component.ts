import { Component, Input, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Router } from '@angular/router';

import { log, getCategory } from '../logging';

import { Connection, Connections, Integrations, UserService } from '@syndesis/ui/platform';
import { ConnectionStore } from '../store/connection/connection.store';

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
    private userService: UserService
  ) {
    this.connections = this.connectionStore.list;
  }

  selectedConnection(connection: Connection) {
    this.router.navigate(['/connections', connection.id]);
  }

  ngOnInit() {
    this.connectionStore.loadAll();
  }
}
