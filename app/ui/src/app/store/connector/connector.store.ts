import { Injectable } from '@angular/core';
import { Connectors, Connector } from '@syndesis/ui/platform';

import { AbstractStore } from '@syndesis/ui/store/entity/entity.store';
import { EventsService } from '@syndesis/ui/store/entity/events.service';
import { ConnectorService } from '@syndesis/ui/store/connector/connector.service';

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
