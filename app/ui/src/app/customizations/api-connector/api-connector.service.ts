import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { ApiHttpService } from '@syndesis/ui/platform';
import {
  ApiConnectors,
  ApiConnectorData, CustomConnectorRequest
} from './api-connector.models';

@Injectable()
export class ApiConnectorService {
  constructor(private apiHttpService: ApiHttpService) { }

  getApiConnector(id: string): Observable<ApiConnectorData> {
    return this.apiHttpService
      .setEndpointUrl('getApiConnectorDetails', { id })
      .get<ApiConnectorData>();
  }

  getApiConnectorList(): Observable<ApiConnectors> {
    return this.apiHttpService
      .setEndpointUrl('getApiConnectorList', { template: 'swagger-connector-template'})
      .get<{ items: ApiConnectors }>() // TODO: Provide a better <T> typing here
      .map(response => response.items);
  }

  submitCustomConnectorInfo(customConnectorRequest: CustomConnectorRequest): Observable<ApiConnectorData> {
    return this.apiHttpService
      .setEndpointUrl('submitCustomConnectorInfo')
      .post<ApiConnectorData>(customConnectorRequest);
  }

  createCustomConnector(customConnectorRequest: CustomConnectorRequest): Observable<any> {
    const apiHttpService = this.apiHttpService.setEndpointUrl('submitCustomConnector');
    const [icon, connectorSettings] = [customConnectorRequest.file, customConnectorRequest];

    if (icon) {
      return apiHttpService.upload({ icon }, { connectorSettings });
    } else {
      return apiHttpService.post(connectorSettings);
    }
  }
}
