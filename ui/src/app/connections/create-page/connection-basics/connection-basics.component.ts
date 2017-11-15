import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { TourService } from 'ngx-tour-ngx-bootstrap';

import { CurrentConnectionService } from '../current-connection';
import { Connectors, Connector, TypeFactory } from '../../../model';
import { ConnectorStore } from '../../../store/connector/connector.store';
import { UserService } from '../../../common/user.service';

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
    private connectorStore: ConnectorStore,
    public tourService: TourService,
    private userService: UserService
  ) {
    this.loading = connectorStore.loading;
    this.connectors = connectorStore.list;
  }

  ngOnInit() {
    this.connectorStore.loadAll();

    /**
     * If guided tour state is set to be shown (i.e. true), then show it for this page, otherwise don't.
     */
    if (this.userService.getTourState() === true) {
      this.tourService.initialize([ {
          anchorId: 'connections.type',
          title: 'Connection',
          content:
            'A connection represents a specific application that you want to obtain data from or send data to.',
          placement: 'top',
        } ]
      );
      setTimeout(() => this.tourService.start());
    }
  }

  onSelected(connector: Connector) {
    const connection = TypeFactory.createConnection();
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
