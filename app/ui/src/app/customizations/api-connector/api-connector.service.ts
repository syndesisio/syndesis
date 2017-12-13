import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Restangular } from 'ngx-restangular';

import { RESTService } from '../../store/entity/rest.service';
import { ApiConnector, ApiConnectors } from './api-connector.models';

@Injectable()
export class ApiConnectorService extends RESTService<ApiConnector, ApiConnectors> {
  constructor(restangular: Restangular) {
    super(restangular.service('connectors?query=connectorGroupId%3Dswagger-connector-template'), 'apiConnector');
  }

  public list(): Observable<ApiConnectors> {
    return super.list().map(apiConnectors => {
      return apiConnectors;
    });
  }
}
