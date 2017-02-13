import { Component, Input, Output, OnInit, EventEmitter } from '@angular/core';

import { log, getCategory } from '../../logging';
import { Connections, Connection } from '../../store/connection/connection.model';

const category = getCategory('Connections');

@Component({
  selector: 'ipaas-connections-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss'],
})
export class ConnectionsListComponent implements OnInit {

  truncateLimit = 80;
  truncateTrail = '…';
  selectedId = undefined;

  @Input() connections: Connections;
  @Input() loading: boolean;
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
