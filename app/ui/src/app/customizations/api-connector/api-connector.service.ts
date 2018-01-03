import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { ApiHttpService } from '@syndesis/ui/platform';
import {
  ApiConnectorData, ApiConnectors,
  ApiConnectorValidation, CustomSwaggerConnectorRequest
} from './api-connector.models';

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
      .get<{ items: ApiConnectors }>() // TODO: Provide a better <T> typing here
      .map(response => response.items);
  }

  submitCustomConnectorInfo(customSwaggerConnectorRequest: CustomSwaggerConnectorRequest): Observable<ApiConnectorValidation> {
    return this.apiHttpService
      .setEndpointUrl('submitCustomConnectorInfo')
      .post<ApiConnectorValidation>(customSwaggerConnectorRequest);
  }

  createCustomConnector(customSwaggerConnectorRequest: CustomSwaggerConnectorRequest): Observable<any> {
    const apiHttpService = this.apiHttpService.setEndpointUrl('submitCustomConnector');
    const [icon, connectorSettings] = [customSwaggerConnectorRequest.file, customSwaggerConnectorRequest];

    if (icon) {
      return apiHttpService.upload({ icon }, { connectorSettings });
    } else {
      return apiHttpService.post(connectorSettings);
    }
  }
}
