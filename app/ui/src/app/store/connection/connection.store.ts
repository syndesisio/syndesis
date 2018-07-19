import { Injectable } from '@angular/core';
import { Connections, Connection } from '@syndesis/ui/platform';
import { ConnectionService } from '@syndesis/ui/store/connection/connection.service';

import { AbstractStore } from '@syndesis/ui/store/entity/entity.store';
import { EventsService } from '@syndesis/ui/store/entity/events.service';

@Injectable()
export class ConnectionStore extends AbstractStore<
  Connection,
  Connections,
  ConnectionService
> {
  private apiEndpoint: String;

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
