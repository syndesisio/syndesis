import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { log, getCategory } from '../logging';

import { Connection, Connections } from '../model';
import { ConnectionStore } from '../store/connection/connection.store';

const category = getCategory('Dashboard');

@Component({
  selector: 'ipaas-dashboard-connections',
  templateUrl: './connections.component.html',
  styleUrls: [],
})
export class DashboardConnectionsComponent implements OnInit {

  connections: Observable<Connections>;
  loading: Observable<boolean>;
  selectedId = undefined;
  truncateLimit = 80;
  truncateTrail = '…';

  constructor(private store: ConnectionStore) {
    this.connections = this.store.list;
    this.loading = this.store.loading;
  }

  onSelect(connection: Connection) {
    log.debugc(() => 'Selected connection (list): ' + connection.name, category);
  }

  isSelected(connection: Connection) {
    return connection.id === this.selectedId;
  }

  ngOnInit() {
    this.store.loadAll();
  }

}
