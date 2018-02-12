import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

import { log, getCategory } from '@syndesis/ui/logging';
import { Connection, Connections } from '@syndesis/ui/platform';
import { ConnectionStore } from '@syndesis/ui/store';

const category = getCategory('Dashboard');

@Component({
  selector: 'syndesis-dashboard-connections',
  templateUrl: './dashboard-connections.component.html',
  styleUrls: ['./dashboard-connections.component.scss']
})
export class DashboardConnectionsComponent implements OnInit {
  @Input() connections: Connections;
  @Input() loading: boolean;
  @Output() selectedConnection = new EventEmitter<Connection>();
  selectedId: string;
  truncateTrail = '…';

  onSelect(connection: Connection) {
    log.debugc(
      () => 'Selected connection (list): ' + connection.name,
      category
    );
    this.selectedId = connection.id;
    this.selectedConnection.emit(connection);
  }

  ngOnInit() {
    log.debugc(
      () =>
        'Got connections: ' + JSON.stringify(this.connections, undefined, 2),
      category
    );
  }
}
