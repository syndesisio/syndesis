import { Injectable } from '@angular/core';

import { ApiHttpService, Action, Actions } from '@syndesis/ui/platform';
import { RESTService } from '@syndesis/ui/store/entity';
import { ConfigService } from '@syndesis/ui/config.service';

@Injectable()
export class ActionService extends RESTService<Action, Actions> {
  constructor(apiHttpService: ApiHttpService, configService: ConfigService) {
    super(apiHttpService, 'actions', 'action', configService);
  }

}
