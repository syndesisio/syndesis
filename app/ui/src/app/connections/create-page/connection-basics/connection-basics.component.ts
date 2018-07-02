import { Component, OnInit } from '@angular/core';
import { Observable, Subject, BehaviorSubject } from 'rxjs';

import { Connectors, Connector, Connection } from '@syndesis/ui/platform';
import { TypeFactory } from '@syndesis/ui/model';
import { CurrentConnectionService } from '../current-connection';
import { ConnectorStore } from '../../../store/connector/connector.store';

@Component({
  selector: 'syndesis-connections-connection-basics',
  templateUrl: 'connection-basics.component.html'
})
export class ConnectionsConnectionBasicsComponent implements OnInit {
  loading: Observable<boolean>;
  connectors: Observable<Connectors>;
  filteredConnectors: Subject<Connectors> = new BehaviorSubject(<Connectors>{});

  constructor(
    private current: CurrentConnectionService,
    private connectorStore: ConnectorStore
  ) {
    this.loading = connectorStore.loading;
    this.connectors = connectorStore.list;
  }

  ngOnInit() {
    this.connectorStore.loadAll();
  }

  onSelected(connector: Connector) {
    const connection = TypeFactory.create<Connection>();
    const plain = connector['plain'];
    if (plain && typeof plain === 'function') {
      connection.connector = plain();
    } else {
      connection.connector = connector;
    }
    connection.icon = connector.icon;
    connection.connectorId = connector.id;
    this.current.connection = connection;
  }
}
