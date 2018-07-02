import { Injectable } from '@angular/core';
import { ValidationErrors } from '@angular/forms';

import { ApiHttpService, Connection, Connections } from '@syndesis/ui/platform';
import { TypeFactory } from '@syndesis/ui/model';
import { RESTService } from '../entity';

@Injectable()
export class ConnectionService extends RESTService<Connection, Connections> {
  constructor(apiHttpService: ApiHttpService) {
    super(apiHttpService, 'connections', 'connection');
  }

  validateName(name: string): Promise<ValidationErrors | null> {
    const connection = TypeFactory.create<Connection>();
    connection.name = name;

    return this.apiHttpService
      .setEndpointUrl('/connections/validation')
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
