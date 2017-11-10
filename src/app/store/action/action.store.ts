import { Injectable } from '@angular/core';
import { ActionService } from './action.service';
import { Actions, Action } from '../../model';

import { AbstractStore } from '../entity/entity.store';
import { EventsService } from '../entity/events.service';

@Injectable()
export class ActionStore extends AbstractStore<Action, Actions, ActionService> {
  constructor(actionService: ActionService, eventService: EventsService) {
    super(actionService, eventService, [], <Action>{});
  }

  protected get kind() {
    return 'Action';
  }
}
