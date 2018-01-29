import { Injectable } from '@angular/core';
import { Restangular } from 'ngx-restangular';

import { Action, Actions } from '@syndesis/ui/platform';
import { RESTService } from '../entity/rest.service';

@Injectable()
export class ActionService extends RESTService<Action, Actions> {
  constructor(restangular: Restangular) {
    super(restangular.service('actions'), 'action');
  }
}
