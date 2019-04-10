import { Injectable } from '@angular/core';
import { ValidationErrors } from '@angular/forms';

import { ApiHttpService, Connection, Connections } from '@syndesis/ui/platform';
import { TypeFactory } from '@syndesis/ui/model';
import { RESTService } from '@syndesis/ui/store/entity';
import { ConfigService } from '@syndesis/ui/config.service';

@Injectable()
export class ConnectionService extends RESTService<Connection, Connections> {
  constructor(apiHttpService: ApiHttpService, configService: ConfigService) {
    super(apiHttpService, 'connections', 'connection', configService);
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
