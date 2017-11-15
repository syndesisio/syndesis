import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';

import { log, getCategory } from '../logging';

import { Connection, Connections } from '../model';
import { ConnectionStore } from '../store/connection/connection.store';

const category = getCategory('Dashboard');

@Component({
  selector: 'syndesis-dashboard-connections',
  templateUrl: './connections.component.html',
  styleUrls: ['./connections.component.scss']
})
export class DashboardConnectionsComponent implements OnInit {
  selectedId = undefined;

  @Input() connections: Connections;
  @Input() loading: boolean;
  @Output() selectedConnection: EventEmitter<Connection> = new EventEmitter();

  truncateTrail = '…';

  //-----  Selecting a Connection ------------------->>

  onSelect(connection: Connection) {
    log.debugc(
      () => 'Selected connection (list): ' + connection.name,
      category
    );
    this.selectedId = connection.id;
    this.selectedConnection.emit(connection);
  }

  //----- Initialization ------------------->>

  ngOnInit() {
    log.debugc(
      () =>
        'Got connections: ' + JSON.stringify(this.connections, undefined, 2),
      category
    );
  }
}
