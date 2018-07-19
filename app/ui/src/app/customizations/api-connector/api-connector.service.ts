import { map } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiHttpService } from '@syndesis/ui/platform';
import {
  ApiConnectors,
  ApiConnectorData,
  CustomConnectorRequest
} from '@syndesis/ui/customizations/api-connector/api-connector.models';

import { apiConnectorEndpoints } from '@syndesis/ui/customizations/api-connector/api-connector.api';

@Injectable()
export class ApiConnectorService {
  constructor(private apiHttpService: ApiHttpService) {}

  getCustomConnector(id: string): Observable<ApiConnectorData> {
    return this.apiHttpService
      .setEndpointUrl(apiConnectorEndpoints.selectApiConnector, { id })
      .get<ApiConnectorData>();
  }

  getCustomConnectorList(): Observable<ApiConnectors> {
    return this.apiHttpService
      .setEndpointUrl(apiConnectorEndpoints.getApiConnectorList, {
        template: 'swagger-connector-template'
      })
      .get<{ items: ApiConnectors }>()
      .pipe(
        // TODO: Provide a better <T> typing here
        map(response => response.items)
      );
  }

  validateCustomConnectorInfo(
    customConnectorRequest: CustomConnectorRequest
  ): Observable<ApiConnectorData> {
    const apiHttpService = this.apiHttpService.setEndpointUrl(
      apiConnectorEndpoints.validateCustomConnectorInfo
    );
    const { specificationFile, connectorTemplateId } = customConnectorRequest;
    if (specificationFile) {
      return apiHttpService.upload<ApiConnectorData>(
        {
          specification: specificationFile
        },
        {
          connectorSettings: { connectorTemplateId }
        }
      );
    } else {
      return apiHttpService.post<ApiConnectorData>(customConnectorRequest);
    }
  }

  createCustomConnector(
    customConnectorRequest: CustomConnectorRequest
  ): Observable<any> {
    const apiHttpService = this.apiHttpService.setEndpointUrl(
      apiConnectorEndpoints.submitCustomConnector
    );
    const [rawConnectorSettings, icon, specification] = [
      customConnectorRequest,
      customConnectorRequest.iconFile,
      customConnectorRequest.specificationFile
    ];

    const connectorSettings = this.sanitizeCustomConnectorRequest(
      rawConnectorSettings
    );

    if (specification || icon) {
      const payload = {};
      if (specification) {
        payload['specification'] = specification;
      }
      if (icon) {
        payload['icon'] = icon;
      }
      return apiHttpService.upload(payload, { connectorSettings });
    } else {
      return apiHttpService.post(connectorSettings);
    }
  }

  updateCustomConnector(
    customConnectorRequest: CustomConnectorRequest
  ): Observable<any> {
    const apiHttpService = this.apiHttpService.setEndpointUrl(
      apiConnectorEndpoints.selectApiConnector,
      { id: customConnectorRequest.id }
    );
    const [rawConnector, icon] = [
      customConnectorRequest,
      customConnectorRequest.iconFile
    ];

    const connector = this.sanitizeCustomConnectorRequest(rawConnector);

    if (icon) {
      return apiHttpService.upload({ icon }, { connector }, { method: 'PUT' });
    } else {
      return apiHttpService.put(connector);
    }
  }

  deleteCustomConnector(id: string): Observable<any> {
    return this.apiHttpService
      .setEndpointUrl(apiConnectorEndpoints.selectApiConnector, { id })
      .delete<any>();
  }

  // Custom connector requests need to be sanitized prior to be submitted to the API
  // Please read: https://github.com/syndesisio/syndesis/issues/980
  private sanitizeCustomConnectorRequest(
    customConnector: CustomConnectorRequest
  ): CustomConnectorRequest {
    const configuredProperties = { ...customConnector.configuredProperties };

    for (const key in configuredProperties) {
      if (configuredProperties.hasOwnProperty(key)) {
        if (
          configuredProperties[key] == null ||
          configuredProperties[key] == undefined ||
          configuredProperties[key] === ''
        ) {
          delete configuredProperties[key];
        }
      }
    }

    return { ...customConnector, configuredProperties };
  }
}
