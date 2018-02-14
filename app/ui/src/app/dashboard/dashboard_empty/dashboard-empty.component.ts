import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { log, getCategory } from '@syndesis/ui/logging';
import { Connection, Connections, Integrations, UserService } from '@syndesis/ui/platform';
import { ConnectionStore } from '@syndesis/ui/store/connection';

const category = getCategory('Dashboard');

@Component({
  selector: 'syndesis-dashboard-empty',
  templateUrl: './dashboard-empty.component.html',
  styleUrls: ['./dashboard-empty.component.scss']
})
export class DashboardEmptyComponent implements OnInit {
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
