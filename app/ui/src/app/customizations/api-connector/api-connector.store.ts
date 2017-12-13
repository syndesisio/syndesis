import { Injectable } from '@angular/core';
import { ApiConnectorService } from './api-connector.service';
import { AbstractStore, EventsService } from '@syndesis/ui/store';

import { ApiConnector, ApiConnectors } from './api-connector.models';

@Injectable()
export class ApiConnectorStore extends AbstractStore<
  ApiConnector,
  ApiConnectors,
  ApiConnectorService
  > {
  constructor(apiConnectorService: ApiConnectorService, eventService: EventsService) {
    super(apiConnectorService, eventService, [], <ApiConnector>{ kind: undefined, data: undefined });
  }

  protected get kind() {
    return 'ApiConnector';
  }

}
