import { Injectable } from '@angular/core';
import { Restangular } from 'ngx-restangular';

import { RESTService } from '../entity/rest.service';
import { Connection, Connections } from '../../model';

@Injectable()
export class ConnectionService extends RESTService<Connection, Connections> {

  private validationService;

  constructor(restangular: Restangular) {
    super(restangular.service('connections'), 'connection');
    this.validationService = restangular.service('connections/validation');
  }

  validate(connection: Connection) {
    return this.validationService.post(connection);
  }

}
