import { Injectable } from '@angular/core';
import { ConnectorService } from './connector.service';
import { Connectors, Connector } from '../../model';

import { AbstractStore } from '../entity/entity.store';
import { EventsService } from '../entity/events.service';

@Injectable()
export class ConnectorStore extends AbstractStore<
  Connector,
  Connectors,
  ConnectorService
> {
  constructor(ConnectorService: ConnectorService, eventService: EventsService) {
    super(ConnectorService, eventService, [], <Connector>{});
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
}
