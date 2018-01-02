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
    return this.apiHttpService
      .setEndpointUrl('submitCustomConnector')
      .upload<any>({
        icon: customSwaggerConnectorRequest.file
      }, {
        connectorSettings: customSwaggerConnectorRequest
      });
  }
}
