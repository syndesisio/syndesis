import { Injectable, EventEmitter } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';

import { Integration } from '../../store/integration/integration.model';
import { log, getCategory } from '../../logging';

const category = getCategory('CurrentFlow');

export class FlowEvent {
  kind: string;
  [name: string]: any;
}

@Injectable()
export class CurrentFlow {

  private _integration: Integration;
  private subscription: Subscription;

  events = new EventEmitter<FlowEvent>();

  constructor() {
    this.subscription = this.events.subscribe((event: FlowEvent) => this.handleEvent(event));
  }

  handleEvent(event: FlowEvent) {
    log.debugc(() => 'event: ' + JSON.stringify(event, undefined, 2), category);
    switch (event.kind) {
      case 'integration-set-connection':
      if (!this._integration.steps) {
        this._integration.steps = [];
      }
      const position = +event['position'];
      const connection = event['connection'];
      this._integration.steps[position] = connection;
      log.debugc(() => 'Set connection ' + connection.name + ' at position: ' + position, category);
      break;
    }
    log.debugc(() => 'integration: ' + JSON.stringify(this._integration, undefined, 2), category);
  }

  get integration() {
    return this._integration;
  }

  set integration(i: Integration) {
    this._integration = i;
    log.debugc(() => 'Integration reset for current flow', category);
    this.events.emit({
      kind: 'integration-updated',
      integration: this._integration,
    });
    if (!i.steps || !i.steps.length) {
      log.debugc(() => 'Integration has no steps, assuming it\'s new', category);
      this.events.emit({
        kind: 'integration-no-connections',
      });
    }
  }


}
