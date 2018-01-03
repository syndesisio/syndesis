import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { ApiConnectorData, ApiConnectors } from './api-connector.models';
import { ApiHttpService } from '@syndesis/ui/platform';

@Injectable()
export class ApiConnectorService {
  constructor(private apiHttpService: ApiHttpService) { }

  getApiConnector(id): Observable<ApiConnectorData> {
    return this.apiHttpService
      .setEndpointUrl('getApiConnectorDetails', { id })
      .get<ApiConnectorData>();
  }

  list(): Observable<ApiConnectors> {
    return this.apiHttpService
      .setEndpointUrl('getApiConnectorList', { template: 'swagger-connector-template'})
      .get<{ items: ApiConnectors }>() // TODO: Provide a better typing T here
      .map(response => response.items);
  }
}
