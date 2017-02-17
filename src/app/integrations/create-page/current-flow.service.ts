import { Injectable, EventEmitter } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { Observable } from 'rxjs/Observable';

import { IntegrationStore } from '../../store/integration/integration.store';
import { Integration, Step, Connection } from '../../model';
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

  constructor(
    private store: IntegrationStore,
    ) {
    this.subscription = this.events.subscribe((event: FlowEvent) => this.handleEvent(event));
  }

  isValid() {
    // TODO more validations on the integration
    return (this._integration.name && this._integration.name.length);
  }

  private createSteps() {
    if (!this._integration.steps) {
      this._integration.steps = [];
    }
  }

  getStep(position: number): Step | Connection {
    if (!this._integration) {
      return undefined;
    }
    this.createSteps();
    const step = this._integration.steps[position];
    if (!step) {
      return undefined;
    }
    if (step.kind === 'connection') {
      return this._integration.connections[position];
    } else {
      return step;
    }
  }

  isEmpty(): boolean {
    if (!this._integration) {
      return true;
    }
    this.createSteps();
    return this._integration.steps.length === 0;
  }

  atEnd(position: number): boolean {
    if (!this._integration) {
      return true;
    }
    this.createSteps();
    return position >= this._integration.steps.length;
  }

  handleEvent(event: FlowEvent): void {
    log.debugc(() => 'event: ' + JSON.stringify(event, undefined, 2), category);
    switch (event.kind) {
      case 'integration-set-connection':
      this.createSteps();
      const position = +event['position'];
      let connection = event['connection'];
      if (connection.plain && typeof connection.plain === 'function') {
        connection = connection.plain();
      }
      this._integration.connections[position] = connection;
      this._integration.steps[position] = <Step> {
        configuredProperties: connection['configuredProperties'],
        id: connection['id'],
        kind: 'connection',
      };
      log.debugc(() => 'Set connection ' + connection.name + ' at position: ' + position, category);
      break;
      case 'integration-set-name':
      this._integration.name = event['name'];
      break;
      case 'integration-save':
        log.debugc(() => 'Saving integration: ' + this._integration);
        // poor man's clone in case we need to munge the data
        const integration = JSON.parse(JSON.stringify(this._integration));
        // TODO munging connection objects for now
        /*
        const steps = integration.steps;
        const newSteps = [];
        for (const step of steps) {
          newSteps.push();
        }
        integration.steps = newSteps;
        integration.connections = steps;
        */
        this.store.create(integration).subscribe((i: Integration) => {
          log.debugc(() => 'Saved integration: ' + JSON.stringify(i, undefined, 2), category);
          const action = event['action'];
          if (action && typeof action === 'function') {
            action(i);
          }
        }, (reason: any) => {
          log.debugc(() => 'Error saving integration: ' + JSON.stringify(reason, undefined, 2), category);
          const errorAction = event['error'];
          if (errorAction && typeof errorAction === 'function') {
            errorAction(reason);
          }
        });
      break;
    }
    // log.debugc(() => 'integration: ' + JSON.stringify(this._integration, undefined, 2), category);
  }

  get integration(): Integration {
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
