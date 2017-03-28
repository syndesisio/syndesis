import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
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

  @Input() connections: Connections;
  @Input() loading: boolean;
  selectedId = undefined;
  truncateLimit = 80;
  truncateTrail = '…';

  @Output() onSelected: EventEmitter<Connection> = new EventEmitter();

  onSelect(connection: Connection) {
    log.debugc(() => 'Selected connection (list): ' + connection.name, category);
    this.selectedId = connection.id;
    this.onSelected.emit(connection);
  }

  isSelected(connection: Connection) {
    return connection.id === this.selectedId;
  }

  ngOnInit() {
    log.debugc(() => 'Got connections: ' + JSON.stringify(this.connections, undefined, 2), category);
  }

}
