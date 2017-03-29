import { Component, Input, OnInit } from '@angular/core';

import { log, getCategory } from '../logging';

import { Connection, Connections } from '../model';
import { ConnectionStore } from '../store/connection/connection.store';

const category = getCategory('Dashboard');

@Component({
  selector: 'ipaas-dashboard-connections',
  templateUrl: './connections.component.html',
  styleUrls: ['./connections.component.scss'],
})
export class DashboardConnectionsComponent implements OnInit {

  @Input() connections: Connections;
  @Input() loading: boolean;
  truncateLimit = 80;
  truncateTrail = '…';

  toggled(open): void {
    log.debugc(() => 'Dropdown is now: ' + open);
  }

  ngOnInit() {
    log.debugc(() => 'Got connections: ' + JSON.stringify(this.connections, undefined, 2), category);
  }

}
