import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Restangular } from 'ngx-restangular';

import { ApiHttpService } from '@syndesis/ui/platform';
import { RESTService } from '@syndesis/ui/store/entity';
import {
  ApiConnectorData, ApiConnectors,
  ApiConnectorValidation, CustomSwaggerConnectorRequest
} from './api-connector.models';

@Injectable()
export class ApiConnectorService extends RESTService<ApiConnectorData, ApiConnectors> {
  constructor(restangular: Restangular, private apiHttpService: ApiHttpService) {
    super(restangular.service('connectors?query=connectorGroupId%3Dswagger-connector-template'), 'apiConnector');
  }

  public list(): Observable<ApiConnectors> {
    return super.list().map(apiConnectors => {
      return apiConnectors;
    });
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
