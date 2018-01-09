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

  getCustomConnector(id: string): Observable<ApiConnectorData> {
    return this.apiHttpService
      .setEndpointUrl('selectApiConnector', { id })
      .get<ApiConnectorData>();
  }

  getCustomConnectorList(): Observable<ApiConnectors> {
    return this.apiHttpService
      .setEndpointUrl('getApiConnectorList', { template: 'swagger-connector-template' })
      .get<{ items: ApiConnectors }>() // TODO: Provide a better <T> typing here
      .map(response => response.items);
  }

  validateCustomConnectorInfo(customConnectorRequest: CustomConnectorRequest): Observable<ApiConnectorData> {
    const apiHttpService = this.apiHttpService.setEndpointUrl('validateCustomConnectorInfo');
    const { specificationFile, connectorTemplateId } = customConnectorRequest;
    if (specificationFile) {
      return apiHttpService.upload<ApiConnectorData>({
        specification: specificationFile
      }, {
        connectorSettings: { connectorTemplateId }
      });
    } else {
      return apiHttpService.post<ApiConnectorData>(customConnectorRequest);
    }
  }

  createCustomConnector(customConnectorRequest: CustomConnectorRequest): Observable<any> {
    const apiHttpService = this.apiHttpService.setEndpointUrl('submitCustomConnector');
    const [connectorSettings, icon, specification] = [
      customConnectorRequest,
      customConnectorRequest.iconFile,
      customConnectorRequest.specificationFile
    ];

    if (specification || icon) {
      const payload = specification && icon ? { specification, icon } : { specification } || { icon };
      return apiHttpService.upload(payload, { connectorSettings });
    } else {
      return apiHttpService.post(connectorSettings);
    }
  }

  updateCustomConnector(customConnectorRequest: CustomConnectorRequest): Observable<any> {
    const apiHttpService = this.apiHttpService.setEndpointUrl('selectApiConnector', { id: customConnectorRequest.id });
    const [connector, icon] = [
      customConnectorRequest,
      customConnectorRequest.iconFile
    ];

    if (icon) {
      return apiHttpService.upload({ icon }, { connector }, 'PUT');
    } else {
      return apiHttpService.put(connector);
    }
  }

  deleteCustomConnector(id: string): Observable<any> {
    return this.apiHttpService
      .setEndpointUrl('selectApiConnector', { id })
      .delete<any>();
  }
}
