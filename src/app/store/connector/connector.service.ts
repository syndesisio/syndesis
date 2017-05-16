import { Injectable } from '@angular/core';
import { Restangular } from 'ngx-restangular';

import { RESTService } from '../entity/rest.service';
import { Connector, Connectors } from '../../model';

@Injectable()
export class ConnectorService extends RESTService<Connector, Connectors> {

  constructor(restangular: Restangular) {
    super(restangular.service('connectors'), 'connector');
  }

  validate(id: string, data: Map<string, string>) {
    return this.restangularService.one(id).one('verifier').customPOST(data);
  }

}
