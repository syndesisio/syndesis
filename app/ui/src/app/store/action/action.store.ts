import { Injectable } from '@angular/core';
import { ActionService } from '@syndesis/ui/store/action/action.service';
import { Actions, Action } from '@syndesis/ui/platform';

import { AbstractStore } from '@syndesis/ui/store/entity/entity.store';
import { EventsService } from '@syndesis/ui/store/entity/events.service';

@Injectable()
export class ActionStore extends AbstractStore<Action, Actions, ActionService> {
  constructor(actionService: ActionService, eventService: EventsService) {
    super(actionService, eventService, [], <Action>{});
  }

  protected get kind() {
    return 'Action';
  }
}
