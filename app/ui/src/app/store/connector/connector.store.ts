import { Injectable } from '@angular/core';
import { Connectors, Connector } from '@syndesis/ui/platform';

import { AbstractStore } from '../entity/entity.store';
import { EventsService } from '../entity/events.service';
import { ConnectorService } from './connector.service';

@Injectable()
export class ConnectorStore extends AbstractStore<
  Connector,
  Connectors,
  ConnectorService
> {
  constructor(connectorService: ConnectorService, eventService: EventsService) {
    super(connectorService, eventService, [], <Connector>{});
  }

  protected get kind() {
    return 'Connector';
  }

  validate(id: string, data: Map<string, string>) {
    return this.service.validate(id, data);
  }

  credentials(id: string) {
    return this.service.credentials(id);
  }

  acquireCredentials(id: string) {
    return this.service.acquireCredentials(id);
  }
}
