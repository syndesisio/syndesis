import { Injectable, EventEmitter } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';

import { Action,
  Connection,
  createIntegration,
  createStep,
  Integration,
  Step,
  key } from '@syndesis/ui/platform';
import { log, getCategory } from '@syndesis/ui/logging';
import { IntegrationStore } from '@syndesis/ui/store';

const category = getCategory('CurrentFlow');

export class FlowEvent {
  kind: string;
  [name: string]: any;
}

function createStepWithId(): Step {
  const step = createStep();
  step.id = key();
  return step;
}

@Injectable()
export class CurrentFlow {
  events = new EventEmitter<FlowEvent>();

  private subscription: Subscription;
  private _integration: Integration;
  private _loaded = false;

  constructor(private store: IntegrationStore) {
    this.subscription = this.events.subscribe((event: FlowEvent) =>
      this.handleEvent(event)
    );
  }

  isValid() {
    // TODO more validations on the integration
    return this.integration.name && this.integration.name.length;
  }

  /**
   * Returns the first step in the integration flow
   *
   * @returns {Step}
   * @memberof CurrentFlow
   */
  getStartStep(): Step {
    return this.getStep(this.getFirstPosition());
  }

  /**
   * Returns the last step in the integration flow
   *
   * @returns {Step}
   * @memberof CurrentFlow
   */
  getEndStep(): Step {
    const lastPosition = this.getLastPosition();
    if (lastPosition < 1) {
      return undefined;
    }
    return this.getStep(lastPosition);
  }

  /**
   * Returns the connection object in the first step in the integration
   *
   * @returns {Connection}
   * @memberof CurrentFlow
   */
  getStartConnection(): Connection {
    const step = this.getStartStep();
    return step ? step.connection : undefined;
  }

  /**
   * Returns the connection object in the last step in the integration
   *
   * @returns {Connection}
   * @memberof CurrentFlow
   */
  getEndConnection(): Connection {
    const step = this.getEndStep();
    return step ? step.connection : undefined;
  }

  /**
   * Returns all steps in between the first and last step in the integration
   *
   * @returns {Array<Step>}
   * @memberof CurrentFlow
   */
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
      return answer.filter(s => s.stepKind === 'endpoint');
    }
    return null;
  }

  getSubsequentStepsWithDataShape(position): Array<Step> {
    const steps = this.getSubsequentSteps(position);
    const answer = steps.filter(s => this.hasDataShape(s, false));
    return answer;
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

  getPreviousStepsWithDataShape(position): Array<Step> {
    const steps = this.getPreviousSteps(position);
    const answer = steps.filter(s => this.hasDataShape(s, true));
    return answer;
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

  getPreviousStepWithDataShape(position): Step {
    const steps = this.getPreviousStepsWithDataShape(position).reverse();
    return steps[0];
  }

  getSubsequentStepWithDataShape(position): Step {
    const steps = this.getSubsequentStepsWithDataShape(position);
    return steps[0];
  }

  /**
   * Returns the initial index in the flow of steps in the integration
   *
   * @returns {number}
   * @memberof CurrentFlow
   */
  getFirstPosition(): number {
    if (!this.integration) {
      return undefined;
    }
    return 0;
  }

  /**
   * Returns the ending index in the flow of steps in the integration
   *
   * @returns {number}
   * @memberof CurrentFlow
   */
  getLastPosition(): number {
    if (!this.integration) {
      return undefined;
    }
    if (this.steps.length <= 1) {
      return 1;
    }
    return this.steps.length - 1;
  }

  /**
   * Returns a position in the middle of the first and last step
   *
   * @returns {number}
   * @memberof CurrentFlow
   */
  getMiddlePosition(): number {
    const last = this.getLastPosition();
    if (last !== undefined) {
      // TODO yes, this
      return Math.round(last / 2);
    } else {
      return undefined;
    }
  }

  /**
   * Returns the step at the given position
   *
   * @param {number} position
   * @returns {Step}
   * @memberof CurrentFlow
   */
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
    switch (event.kind) {
      case 'integration-updated': {
        this._loaded = true;
        setTimeout(() => {
          if (this.isEmpty()) {
            this.events.emit({
              kind: 'integration-no-connections'
            });
          }
        }, 10);
        break;
      }
      case 'integration-insert-step': {
        const position = +event['position'];
        this.insertStepAfter(position);
        this.maybeDoAction(event['onSave']);
        break;
      }
      case 'integration-insert-connection': {
        const position = +event['position'];
        this.insertConnectionAfter(position);
        this.maybeDoAction(event['onSave']);
        break;
      }
      case 'integration-remove-step': {
        {
          const position = +event['position'];
          if (
            position === this.getFirstPosition() ||
            position === this.getLastPosition()
          ) {
            this.steps[position] = createStepWithId();
            this.steps[position].stepKind = 'endpoint';
          } else {
            this.steps.splice(position, 1);
          }
          this.maybeDoAction(event['onSave']);
        }
        break;
      }
      case 'integration-set-step': {
        const position = +event['position'];
        const step = <Step>event['step'];
        this.steps[position] = { ...createStepWithId(), ...step };
        if (this.steps[position].id == undefined) {
          this.steps[position].id = key();
        }
        this.maybeDoAction(event['onSave']);
        log.debugc(() => 'Set step at position: ' + position, category);
        break;
      }
      case 'integration-set-properties': {
        const position = +event['position'];
        const action = event['action'];
        const properties = this.stringifyValues(event['properties']);
        const step = this.steps[position] || createStepWithId();
        step.configuredProperties = properties;
        this.steps[position] = step;
        this.maybeDoAction(event['onSave']);
        log.debugc(
          () =>
            'Set properties at position: ' +
            position +
            ' step: ' +
            JSON.stringify(step),
          category
        );
        break;
      }
      case 'integration-set-action': {
        const position = +event['position'];
        const action = event['action'];
        // TODO no step here should really raise an error
        const step = this.steps[position] || createStepWithId();
        step.action = action;
        step.stepKind = 'endpoint';
        this.steps[position] = step;
        this.maybeDoAction(event['onSave']);
        log.debugc(
          () => 'Set action ' + action.name + ' at position: ' + position,
          category
        );
        break;
      }
      case 'integration-set-connection': {
        const position = +event['position'];
        const connection = event['connection'];
        const step = createStepWithId();
        step.stepKind = 'endpoint';
        step.connection = connection;
        this.steps[position] = step;
        this.maybeDoAction(event['onSave']);
        log.debugc(
          () =>
            'Set connection ' + connection.name + ' at position: ' + position,
          category
        );
        break;
      }
      case 'integration-set-property':
        const property = event['property'];
        let value = event['value'];
        if (property === 'configuredProperties') {
          value = this.stringifyValues(value);
        }
        this._integration[event['property']] = event['value'];
        this.maybeDoAction(event['onSave']);
        break;
      case 'integration-save': {
        log.debugc(() => 'Saving integration: ' + this.integration);
        // poor man's clone in case we need to munge the data
        const integration = this.getIntegrationClone();
        const tags = integration.tags || [];
        const connectorIds = this.getSubsequentConnections(0).map(
          step => step.connection.connectorId
        );
        connectorIds.forEach(id => {
          if (tags.indexOf(id) === -1) {
            tags.push(id);
          }
        });
        integration.tags = tags;
        const sub = this.store.updateOrCreate(integration).subscribe(
          (i: Integration) => {
            log.debugc(
              () => 'Saved integration: ' + JSON.stringify(i, undefined, 2),
              category
            );
            const action = event['action'];
            if (action && typeof action === 'function') {
              action(i);
            }
            sub.unsubscribe();
          },
          (reason: any) => {
            log.infoc(
              () =>
                'Error saving integration: ' +
                JSON.stringify(reason, undefined, 2),
              category
            );
            const errorAction = event['error'];
            if (errorAction && typeof errorAction === 'function') {
              errorAction(reason);
            }
            sub.unsubscribe();
          }
        );
        break;
      }
      default:
        break;
    }
    // log.debugc(() => 'integration: ' + JSON.stringify(this._integration, undefined, 2), category);
  }

  getIntegrationClone(): Integration {
    return JSON.parse(JSON.stringify(this.integration));
  }

  get loaded(): boolean {
    return this._loaded;
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
    this._loaded = false;
    this._integration = <Integration>i;
    if (i && i.steps && i.steps.length) {
      i.steps = i.steps.filter(step => step !== null);
    }
    setTimeout(() => {
      this.events.emit({
        kind: 'integration-updated',
        integration: this.integration
      });
    }, 10);
  }

  private hasDataShape(step: Step, isInput = false): boolean {
      if (step.stepKind === 'endpoint') {
        return true;
      }
      if (step.stepKind !== 'extension') {
        return false;
      }
      // it's an extesion, we need to look at the action
      const action = step.action;
      const descriptor = action.descriptor;
      const dataShape = isInput ? descriptor.inputDataShape : descriptor.outputDataShape;
      return dataShape.kind !== 'any' && dataShape.kind !== 'none';
  }

  private maybeDoAction(thing: any) {
    if (thing && typeof thing === 'function') {
      thing.call(thing);
    }
  }

  private insertStepAfter(position: number) {
    const target = position + 1;
    const step = createStepWithId();
    step.stepKind = undefined;
    this.steps.splice(target, 0, step);
  }

  private insertConnectionAfter(position: number) {
    const target = position + 1;
    const step = createStepWithId();
    step.stepKind = 'endpoint';
    this.steps.splice(target, 0, step);
  }

  private stringifyValues(_props: any) {
    if (!_props) {
      return _props;
    }
    // let's clone this to be on the safe side
    const props = JSON.parse(JSON.stringify(_props));
    for (const prop of Object.keys(props)) {
      const value = props[prop];
      switch (typeof value) {
        case 'string':
        case 'number':
          continue;
        default:
          props[prop] = JSON.stringify(value);
      }
    }
    return props;
  }
}
