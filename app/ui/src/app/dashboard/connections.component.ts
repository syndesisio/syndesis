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
import { ConfigService } from '@syndesis/ui/config.service';

const category = getCategory('Dashboard');

@Component({
  selector: 'syndesis-dashboard-connections',
  templateUrl: './connections.component.html',
  styleUrls: ['./connections.component.scss']
})
export class DashboardConnectionsComponent implements OnInit {
  readonly apiEndpoint: any;
  selectedId = undefined;

  @Input() connections: Connections;
  @Input() loading: boolean;
  @Output() selectedConnection: EventEmitter<Connection> = new EventEmitter();

  truncateTrail = '…';

  constructor(
    private config: ConfigService
  ) {
    this.apiEndpoint = this.config.getSettings().apiEndpoint;
  }

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

  connectionIcon(connection: Connection) {
    if (connection.icon.startsWith('db:')) {
      return `${this.apiEndpoint}/connectors/${connection.connectorId || connection.id}/icon`;
    }
    return `../../../assets/icons/${connection.connectorId || connection.id}.connection.png`;
  }
}
