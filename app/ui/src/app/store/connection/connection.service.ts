import { Injectable } from '@angular/core';
import { Restangular } from 'ngx-restangular';
import { ValidationErrors } from '@angular/forms';

import { RESTService } from '../entity/rest.service';
import { Connection, Connections, TypeFactory } from '../../model';

@Injectable()
export class ConnectionService extends RESTService<Connection, Connections> {
  private validationService;

  constructor(restangular: Restangular) {
    super(restangular.service('connections'), 'connection');
    this.validationService = restangular.service('connections/validation');
  }

  validateName(name: string): Promise<ValidationErrors | null> {
    const connection = TypeFactory.createConnection();
    connection.name = name;
    return this.validationService
      .post(connection)
      .toPromise()
      .then(response => null)
      .catch(response =>
        response.data.reduce((errors, item) => {
          errors[item.error] = true;
          return errors;
        }, {})
      );
  }
}
