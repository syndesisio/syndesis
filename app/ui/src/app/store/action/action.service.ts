import { Injectable } from '@angular/core';

import { ApiHttpService, Action, Actions } from '@syndesis/ui/platform';
import { RESTService } from '../entity';

@Injectable()
export class ActionService extends RESTService<Action, Actions> {
  constructor(apiHttpService: ApiHttpService) {
    super(apiHttpService, 'actions', 'action');
  }
}
