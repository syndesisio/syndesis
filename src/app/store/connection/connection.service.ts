import { Injectable } from '@angular/core';
import { Restangular } from 'ng2-restangular';

import { RESTService } from '../entity/rest.service';
import { Connection, Connections } from '../../model';

@Injectable()
export class ConnectionService extends RESTService<Connection, Connections> {

  constructor(restangular: Restangular) {
    super(restangular.service('connections'));
  }

}
