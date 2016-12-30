import { Injectable } from '@angular/core';

import { Restangular } from 'ng2-restangular';

import { RESTService } from './rest.service';
import { Integration, Integrations } from './integration.model';

@Injectable()
export class IntegrationsService extends RESTService<Integration, Integrations> {

  private integrationsRestangular: Restangular;

  constructor(restangular: Restangular) {
    super(restangular.service('integrations'));
  }

}
