import { Injectable } from '@angular/core';
import { ConnectionService } from './connection.service';
import { Connections, Connection } from './connection.model';

import { AbstractStore } from '../entity/entity.store';

@Injectable()
export class ConnectionStore extends AbstractStore<Connection, Connections, ConnectionService> {
  constructor(connectionService: ConnectionService) {
    super(connectionService, [], <Connection>{});
  }

  protected get kind() { return 'Connection'; }
}
