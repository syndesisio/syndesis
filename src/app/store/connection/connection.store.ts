import { Injectable } from '@angular/core';
import { ConnectionService } from './connection.service';
import { Connections, Connection } from '../../model';

import { AbstractStore } from '../entity/entity.store';
import { EventsService } from '../entity/events.service';

@Injectable()
export class ConnectionStore extends AbstractStore<
  Connection,
  Connections,
  ConnectionService
> {
  constructor(
    connectionService: ConnectionService,
    eventService: EventsService
  ) {
    super(connectionService, eventService, [], <Connection>{});
  }

  protected get kind() {
    return 'Connection';
  }
}
