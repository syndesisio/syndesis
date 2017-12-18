import { Injectable } from '@angular/core';
import { ApiConnectorService } from './api-connector.service';
import { AbstractStore, EventsService } from '@syndesis/ui/store';

import { ApiConnector, ApiConnectorData, ApiConnectors } from './api-connector.models';

@Injectable()
export class ApiConnectorStore extends AbstractStore<
  ApiConnectorData,
  ApiConnectors,
  ApiConnectorService
  > {
  constructor(apiConnectorService: ApiConnectorService, eventService: EventsService) {
    super(apiConnectorService, eventService, [], <ApiConnectorData>{});
  }

  protected get kind() {
    return 'ApiConnector';
  }

}
