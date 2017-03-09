import { Injectable, EventEmitter } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { Observable } from 'rxjs/Observable';

import { IntegrationStore } from '../../store/integration/integration.store';
import { Action, Integration, Step, Connection } from '../../model';
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

  constructor( private store: IntegrationStore ) {
    this.subscription = this.events.subscribe((event: FlowEvent) => this.handleEvent(event));
  }

  isValid() {
    // TODO more validations on the integration
    return (this.integration.name && this.integration.name.length);
  }

  getStartConnection(): Connection {
    const step = this.getStep(this.getFirstPosition());
    return step ? step.connection : undefined;
  }

  getEndConnection(): Connection {
    const lastPosition = this.getLastPosition();
    if (lastPosition < 1) {
      return undefined;
    }
    const step = this.getStep(this.getLastPosition());
    return step ? step.connection : undefined;
  }

  getMiddleSteps(): Array<Step> {
    if (this.getLastPosition() < 2) {
      return [];
    }
    if (!this.steps) {
      return [];
    }
    return this.steps.slice(1, -1);
  }

  getFirstPosition(): number {
    if (!this.integration) {
      return undefined;
    }
    return 0;
  }

  getLastPosition(): number {
    if (!this.integration) {
      return undefined;
    }
    if (this.steps.length <= 1) {
      return 1;
    }
    return this.steps.length - 1;
  }

  getMiddlePosition(): number {
    const last = this.getLastPosition();
    if (last !== undefined) {
      // TODO yes, this
      return Math.round(last / 2);
    } else {
      return undefined;
    }
  }

  getStep(position: number): Step {
    if (!this.integration) {
      return undefined;
    }
    return this.steps[position];
  }

  isEmpty(): boolean {
    if (!this.integration) {
      return true;
    }
    return this.steps.length === 0;
  }

  atEnd(position: number): boolean {
    if (!this.integration) {
      return true;
    }
    return position >= this.steps.length;
  }

  handleEvent(event: FlowEvent): void {
    log.debugc(() => 'event: ' + JSON.stringify(event, undefined, 2), category);
    switch (event.kind) {
      case 'integration-selected-action':
        let position = +event[ 'position' ];
        let action = event[ 'action' ];
        //this.actions[ position ] = action;
        this.steps[ position ] = <Step> {
          action: action,
          configuredProperties: action[ 'properties' ],
          id: action[ 'id' ],
          kind: 'endpoint',
        };
        log.debugc(() => 'Set action ' + action.name + ' at position: ' + position, category);
        break;
      case 'integration-selected-connection':
        position = +event[ 'position' ];
        const connection = event[ 'connection' ];
        this.steps[ position ] = <Step> {
          connection: connection,
          configuredProperties: connection[ 'configuredProperties' ],
          id: connection[ 'id' ],
          kind: 'endpoint',
        };
        log.debugc(() => 'Set connection ' + connection.name + ' at position: ' + position, category);
        break;
      case 'integration-set-name':
        this._integration.name = event[ 'name' ];
        break;
      case 'integration-save':
        log.debugc(() => 'Saving integration: ' + this.integration);
        // poor man's clone in case we need to munge the data
        const integration = JSON.parse(JSON.stringify(this.integration));
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
        const sub = this.store.updateOrCreate(integration).subscribe((i: Integration) => {
          log.debugc(() => 'Saved integration: ' + JSON.stringify(i, undefined, 2), category);
          action = event[ 'action' ];
          if (action && typeof action === 'function') {
            action(i);
          }
          sub.unsubscribe();
        }, (reason: any) => {
          log.debugc(() => 'Error saving integration: ' + JSON.stringify(reason, undefined, 2), category);
          const errorAction = event[ 'error' ];
          if (errorAction && typeof errorAction === 'function') {
            errorAction(reason);
          }
          sub.unsubscribe();
        });
        break;
    }
    // log.debugc(() => 'integration: ' + JSON.stringify(this._integration, undefined, 2), category);
  }

  get integration(): Integration {
    if (!this._integration) {
      return undefined;
    }
    return this._integration;
  }

  get steps(): Array<Step> {
    if (!this._integration) {
      return undefined;
    } else {
      if (!this._integration.steps) {
        this._integration.steps = [];
      }
      return this._integration.steps;
    }
  }

  set integration(i: Integration) {
    this._integration = <Integration> i;
    log.debugc(() => 'Integration reset for current flow', category);
    this.events.emit({
      kind: 'integration-updated',
      integration: this.integration,
    });
    if (!this.steps || !this.steps.length) {
      log.debugc(() => 'Integration has no steps, assuming it\'s new', category);
      this.events.emit({
        kind: 'integration-no-connections',
      });
    }
  }


}
