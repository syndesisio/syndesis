import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Restangular } from 'ngx-restangular';

import { RESTService } from '@syndesis/ui/store/entity';
import { ApiConnectorData, ApiConnectors } from './api-connector.models';
import { ApiHttpService } from '@syndesis/ui/platform';

@Injectable()
export class ApiConnectorService extends RESTService<ApiConnectorData, ApiConnectors> {
  constructor(restangular: Restangular, private apiHttpService: ApiHttpService) {
    super(restangular.service('connectors?query=connectorGroupId%3Dswagger-connector-template'), 'apiConnector');
  }

  public getApiConnector(id): Observable<ApiConnectorData> {
    return this.apiHttpService.setEndpointUrl('/connectors/{id}', { id }).get<ApiConnectorData>();
  }

  public list(): Observable<ApiConnectors> {
    return super.list().map(apiConnectors => {
      return apiConnectors;
    });
  }
}
