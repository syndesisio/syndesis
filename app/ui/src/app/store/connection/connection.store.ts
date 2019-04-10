import { Injectable } from '@angular/core';
import {
  Connections,
  Connection,
  HIDE_FROM_CONNECTION_PAGES,
} from '@syndesis/ui/platform';
import { ConnectionService } from '@syndesis/ui/store/connection/connection.service';

import { AbstractStore } from '@syndesis/ui/store/entity/entity.store';
import { EventsService } from '@syndesis/ui/store/entity/events.service';

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

  get listVisible() {
    // Returns the list of visible connections
    return this.list.map(lst =>
      lst.filter(c => !c.metadata || !c.metadata[HIDE_FROM_CONNECTION_PAGES])
    );
  }
}
