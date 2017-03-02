import { Injectable } from '@angular/core';
import { Restangular } from 'ng2-restangular';

import { RESTService } from '../entity/rest.service';
import { Connector, Connectors } from '../../model';

@Injectable()
export class ConnectorService extends RESTService<Connector, Connectors> {

  constructor(restangular: Restangular) {
    super(restangular.service('connectors'), 'connector');
  }

}
