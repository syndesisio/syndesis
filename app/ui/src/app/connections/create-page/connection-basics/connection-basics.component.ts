import { Component, OnInit } from '@angular/core';
import { Observable, Subject, BehaviorSubject } from 'rxjs';

import { Connectors, Connector, Connection } from '@syndesis/ui/platform';
import { CurrentConnectionService } from '@syndesis/ui/connections/create-page/current-connection';
import { ConnectorStore } from '@syndesis/ui/store/connector/connector.store';

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
    this.connectors = connectorStore.listVisible;
  }

  ngOnInit() {
    this.connectorStore.loadAll();
  }

  onSelected(connector: Connector) {
    const connection = <Connection>{};
    connection.connector = connector;
    connection.icon = connector.icon;
    connection.connectorId = connector.id;
    this.current.connection = connection;
  }
}
