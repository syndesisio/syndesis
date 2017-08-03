import { Injectable, EventEmitter } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';

import { IntegrationStore } from '../../store/integration/integration.store';
import {
  Action,
  Integration,
  Step,
  Connection,
  TypeFactory,
} from '../../model';
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

  constructor(private store: IntegrationStore) {
    this.subscription = this.events.subscribe((event: FlowEvent) =>
      this.handleEvent(event),
    );
  }

  isValid() {
    // TODO more validations on the integration
    return this.integration.name && this.integration.name.length;
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

  /**
   * Return all steps in the flow after the supplied position
   *
   * @param {any} position
   * @returns {Array<Step>}
   * @memberof CurrentFlow
   */
  getSubsequentSteps(position): Array<Step> {
    if (!this._integration) {
      return undefined;
    }
    if (!this._integration.steps) {
      this._integration.steps = [];
    }
    return this._integration.steps.slice(position);
  }

  /**
   * Return all steps in the flow after the supplied position that are connctions
   *
   * @param {any} position
   * @returns {Array<Step>}
   * @memberof CurrentFlow
   */
  getSubsequentConnections(position): Array<Step> {
    const answer = this.getSubsequentSteps(position);
    if (answer) {
      return answer.filter((s) => s.stepKind === 'endpoint');
    }
    return null;
  }

  /**
   * Return all steps in the flow before the supplied position
   *
   * @param {any} position
   * @returns {Array<Step>}
   * @memberof CurrentFlow
   */
  getPreviousSteps(position): Array<Step> {
    if (!this._integration) {
      return undefined;
    } else {
      if (!this._integration.steps) {
        this._integration.steps = [];
      }
      return this._integration.steps.slice(0, position);
    }
  }

  /**
   * Return all steps that are connections in the flow before the supplied position
   *
   * @param {any} position
   * @returns {Array<Step>}
   * @memberof CurrentFlow
   */
  getPreviousConnections(position): Array<Step> {
    const answer = this.getPreviousSteps(position);
    if (answer) {
      return answer.filter(s => s.stepKind === 'endpoint');
    }
    return null;
  }

  /**
   * Return the first connection in the flow before the supplied position
   *
   * @param {any} position
   * @returns {Step}
   * @memberof CurrentFlow
   */
  getPreviousConnection(position): Step {
    const connections = this.getPreviousConnections(position).reverse();
    return connections[0];
  }

  /**
   * Return the first connection in the flow after the supplied position
   *
   * @param {any} position
   * @returns {Step}
   * @memberof CurrentFlow
   */
  getSubsequentConnection(position): Step {
    const connections = this.getSubsequentConnections(position);
    return connections[0];
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

  private maybeDoAction(thing: any) {
    if (thing && typeof thing === 'function') {
      thing();
    }
  }

  handleEvent(event: FlowEvent): void {
    log.debugc(() => 'event: ' + JSON.stringify(event, undefined, 2), category);
    switch (event.kind) {
      case 'integration-remove-step': {
        {
          const position = +event['position'];
          if (
            position === this.getFirstPosition() ||
            position === this.getLastPosition()
          ) {
            this.steps[position] = TypeFactory.createStep();
            this.steps[position].stepKind = 'endpoint';
          } else {
            this.steps.splice(position, 1);
          }
          this.maybeDoAction(event['onSave']);
        }
        break;
      }
      case 'integration-set-step':
        {
          const position = +event['position'];
          const step = <Step>event['step'];
          this.steps[position] = TypeFactory.createStep();
          this.steps[position].stepKind = step.stepKind;
          this.maybeDoAction(event['onSave']);
          log.debugc(() => 'Set step at position: ' + position, category);
        }
        break;
      case 'integration-set-properties':
        {
          const position = +event['position'];
          const action = event['action'];
          const properties = event['properties'];
          const step = this.steps[position] || TypeFactory.createStep();
          step.configuredProperties = properties;
          this.steps[position] = step;
          this.maybeDoAction(event['onSave']);
          log.debugc(() => 'Set properties at position: ' + position, category);
        }
        break;
      case 'integration-set-action':
        {
          const position = +event['position'];
          const action = event['action'];
          // TODO no step here should really raise an error
          const step = this.steps[position] || TypeFactory.createStep();
          step.action = action;
          step.stepKind = 'endpoint';
          this.steps[position] = step;
          this.maybeDoAction(event['onSave']);
          log.debugc(
            () => 'Set action ' + action.name + ' at position: ' + position,
            category,
          );
        }
        break;
      case 'integration-set-connection':
        {
          const position = +event['position'];
          const connection = event['connection'];
          const step = TypeFactory.createStep();
          step.stepKind = 'endpoint';
          step.connection = connection;
          this.steps[position] = step;
          this.maybeDoAction(event['onSave']);
          log.debugc(
            () =>
              'Set connection ' + connection.name + ' at position: ' + position,
            category,
          );
        }
        break;
      case 'integration-set-property':
        this._integration[event['property']] = event['value'];
        this.maybeDoAction(event['onSave']);
        break;
      case 'integration-save':
        {
          log.debugc(() => 'Saving integration: ' + this.integration);
          // poor man's clone in case we need to munge the data
          const integration = this.getIntegrationClone();
          const sub = this.store.updateOrCreate(integration).subscribe(
            (i: Integration) => {
              log.debugc(
                () => 'Saved integration: ' + JSON.stringify(i, undefined, 2),
                category,
              );
              const action = event['action'];
              if (action && typeof action === 'function') {
                action(i);
              }
              sub.unsubscribe();
            },
            (reason: any) => {
              log.debugc(
                () =>
                  'Error saving integration: ' +
                  JSON.stringify(reason, undefined, 2),
                category,
              );
              const errorAction = event['error'];
              if (errorAction && typeof errorAction === 'function') {
                errorAction(reason);
              }
              sub.unsubscribe();
            },
          );
        }
        break;
    }
    // log.debugc(() => 'integration: ' + JSON.stringify(this._integration, undefined, 2), category);
  }

  getIntegrationClone(): Integration {
    return JSON.parse(JSON.stringify(this.integration));
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
    this._integration = <Integration>i;
    this.events.emit({
      kind: 'integration-updated',
      integration: this.integration,
    });
    if (!this.steps || !this.steps.length) {
      this.events.emit({
        kind: 'integration-no-connections',
      });
    }
  }
}
