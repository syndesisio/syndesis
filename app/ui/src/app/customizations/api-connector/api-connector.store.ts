import { Injectable } from '@angular/core';
import { ApiConnectorService } from './api-connector.service';
import { ApiConnector, ApiConnectors } from './api-connector.model';
import { AbstractStore } from '../../store/entity/entity.store';
import { EventsService } from '../../store/entity/events.service';

@Injectable()
export class ApiConnectorStore extends AbstractStore<
  ApiConnector,
  ApiConnectors,
  ApiConnectorService
> {
  constructor(extensionService: ApiConnectorService, eventService: EventsService) {
    super(extensionService, eventService, [], <ApiConnector>{ name: undefined });
  }

  protected get kind() {
    return 'ApiConnector';
  }

}
