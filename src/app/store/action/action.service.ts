import { Injectable } from '@angular/core';
import { Restangular } from 'ng2-restangular';

import { RESTService } from '../entity/rest.service';
import { Action, Actions } from '../../model';

@Injectable()
export class ActionService extends RESTService<Action, Actions> {

  constructor(restangular: Restangular) {
    super(restangular.service('actions'), 'action');
  }

}
