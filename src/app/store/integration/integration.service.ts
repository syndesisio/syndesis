import { Injectable } from '@angular/core';

import { Restangular } from 'ng2-restangular';

import { RESTService } from '../entity/rest.service';
import { Integration, Integrations } from './integration.model';

@Injectable()
export class IntegrationService extends RESTService<Integration, Integrations> {

  constructor(restangular: Restangular) {
    super(restangular.service('integrations'));
  }

}
