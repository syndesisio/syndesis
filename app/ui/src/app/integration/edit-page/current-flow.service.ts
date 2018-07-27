import { Injectable, EventEmitter } from '@angular/core';
import { Observable, Subscription } from 'rxjs';

import {
  Action,
  Connection,
  Connections,
  createIntegration,
  createStep,
  createConnectionStep,
  DataShapeKinds,
  DataShape,
  Integration,
  Step,
  key,
  IntegrationSupportService,
  ActionDescriptor
} from '@syndesis/ui/platform';
import { log, getCategory } from '@syndesis/ui/logging';
import {
  IntegrationStore,
  ENDPOINT,
  DATA_MAPPER,
  EXTENSION,
  BASIC_FILTER,
  StepStore
} from '@syndesis/ui/store';
import { FlowEvent } from '@syndesis/ui/integration/edit-page';

const category = getCategory('CurrentFlow');

@Injectable()
export class CurrentFlowService {
  events = new EventEmitter<FlowEvent>();

  private subscription: Subscription;
  private _integration: Integration;
  private _loaded = false;

  constructor(
    private integrationStore: IntegrationStore,
    private stepStore: StepStore,
    private integrationSupportService: IntegrationSupportService
  ) {
    this.subscription = this.events.subscribe((event: FlowEvent) =>
      this.handleEvent(event)
    );
  }

  isValid(): boolean {
    // TODO more validations on the integration
    return this.integration.name && this.integration.name.length > 0;
  }

  /**
   * Returns the connections suitable for the given position in the integration flow
   * @param connections
   * @param position
   */
  filterConnectionsByPosition(connections: Connections, position: number) {
    if (position === undefined) {
      // safety net
      return connections;
    }
    if (position === 0) {
      return connections.filter(connection => {
        if (!connection.connector) {
          // safety net
          return true;
        }
        return connection.connector.actions.some(action => {
          return action.pattern === 'From';
        });
      });
    }
    return connections.filter(connection => {
      if (!connection.connector) {
        // safety net
        return true;
      }
      return connection.connector.actions.some(action => {
        return action.pattern === 'To';
      });
    });
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
      return answer.filter(s => s.stepKind === ENDPOINT);
    }
    return null;
  }

  /**
   * Return all DataShape aware steps after the supplied position.
   * @param {number} position
   * @returns {Array<{step: Step, index: number}>}
   */
  getSubsequentStepsWithDataShape(
    position
  ): Array<{ step: Step; index: number }> {
    const answer: { step: Step; index: number }[] = [];
    const steps = this.getSubsequentSteps(position);
    if (steps) {
      steps.forEach((step, index) => {
        if (this.hasDataShape(step, true)) {
          answer.push({ step: step, index: position + index });
        }
      });
    }
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

  /**
   * Return all DataShape aware steps before the supplied position.
   * @param {number} position
   * @returns {Array<{step: Step, index: number}>}
   */
  getPreviousStepsWithDataShape(
    position
  ): Array<{ step: Step; index: number }> {
    const answer: { step: Step; index: number }[] = [];
    this.getPreviousSteps(position).forEach((step, index) => {
      if (this.hasDataShape(step, false)) {
        answer.push({ step, index });
      }
    });
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
      return answer.filter(s => s.stepKind === ENDPOINT);
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

  getPreviousStepIndexWithDataShape(position): number {
    const steps = this.getPreviousStepsWithDataShape(position).reverse();
    return steps[0].index;
  }

  getPreviousStepWithDataShape(position): Step {
    const steps = this.getPreviousStepsWithDataShape(position).reverse();
    return steps[0].step;
  }

  getSubsequentStepWithDataShape(position): Step {
    const steps = this.getSubsequentStepsWithDataShape(position);
    return steps[0].step;
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
    // position is assumed to be 0 indexed
    return position + 1 >= this.steps.length;
  }

  isActionShapeless(descriptor: ActionDescriptor) {
    if (!descriptor) {
      return false;
    }
    const inputDataShape = descriptor.inputDataShape;
    const outputDataShape = descriptor.outputDataShape;
    return (
      inputDataShape.kind === DataShapeKinds.ANY ||
      outputDataShape.kind === DataShapeKinds.ANY
    );
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
      case 'integration-insert-datamapper': {
        const position = +event['position'];
        this.insertStepBefore(position, DATA_MAPPER);
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
            this.steps[position] = createStep();
            this.steps[position].stepKind = ENDPOINT;
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
        this.steps[position] = { ...createStep(), ...step };
        if (this.steps[position].id == undefined) {
          this.steps[position].id = key();
        }
        this.maybeDoAction(event['onSave']);
        log.debugc(() => 'Set step at position: ' + position, category);
        break;
      }
      case 'integration-set-metadata': {
        const position = +event['position'];
        const metadata = event['metadata'];
        const step = <any>(this.steps[position] || createStep());
        step.metadata = { ...step.metadata, ...metadata };
        this.maybeDoAction(event['onSave']);
        break;
      }
      case 'integration-set-properties': {
        const position = +event['position'];
        const action = event['action'];
        const properties = this.stringifyValues(event['properties']);
        const step = this.steps[position] || createStep();
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
        const stepKind = event['stepKind'] || ENDPOINT;
        // TODO no step here should really raise an error
        const step = this.steps[position] || createStep();
        // only reset this object if the action is being changed
        if (!step.action || step.action.id !== action.id) {
          step.action = action;
        }
        step.stepKind = stepKind;
        this.steps[position] = step;
        this.maybeDoAction(event['onSave']);
        log.debugc(
          () => 'Set action ' + action.name + ' at position: ' + position,
          category
        );
        break;
      }
      case 'integration-set-descriptor': {
        const position = +event['position'];
        const descriptor = event['descriptor'] as ActionDescriptor;
        const step = this.steps[position] || createStep();
        if (!step.action) {
          step.action = { actionType: 'step' } as Action;
          step.action.descriptor = descriptor;
          return;
        }
        // update the step's configured properties with any defaults defined in the descriptor
        const data = step.configuredProperties || {};
        for (const actionProperty of Object.keys(data)) {
          if (!data[actionProperty]) {
            step.configuredProperties[
              actionProperty
            ] = descriptor.propertyDefinitionSteps.map(actionDefinitionStep => {
              return actionDefinitionStep.properties[actionProperty]
                .defaultValue;
            })[0];
          }
        }
        // set the descriptor but avoid overwriting data shapes if they're user set, or if the incoming datashapes don't have the spec set
        const inputDataShape = step.action.descriptor.inputDataShape;
        const outputDataShape = step.action.descriptor.outputDataShape;
        step.action.descriptor = descriptor;
        if (
          this.isUserDefined(inputDataShape) ||
          (descriptor.inputDataShape.kind !== DataShapeKinds.NONE &&
            !descriptor.inputDataShape.specification)
        ) {
          step.action.descriptor.inputDataShape = inputDataShape;
        }
        if (
          this.isUserDefined(outputDataShape) ||
          (descriptor.outputDataShape.kind !== DataShapeKinds.NONE &&
            !descriptor.outputDataShape.specification)
        ) {
          step.action.descriptor.outputDataShape = outputDataShape;
        }
        this.maybeDoAction(event['onSave']);
        break;
      }
      case 'integration-set-datashape': {
        const position = +event['position'];
        const dataShape = event['dataShape'] as DataShape;
        const isInput = event['isInput'] || false;
        const step = this.steps[position] || createStep();
        if (!step.action) {
          step.action = {} as Action;
          step.action.descriptor = {} as ActionDescriptor;
        }
        if (isInput) {
          step.action.descriptor.inputDataShape = dataShape;
        } else {
          step.action.descriptor.outputDataShape = dataShape;
        }
        this.maybeDoAction(event['onSave']);
        break;
      }
      case 'integration-set-connection': {
        const position = +event['position'];
        const connection = event['connection'];
        const step = createStep();
        step.stepKind = ENDPOINT;
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
        // ensure that all steps have IDs before saving
        this._integration.steps.forEach(s => {
          if (!s.id) {
            s.id = key();
          }
        });
        // poor man's clone in case we need to munge the data
        const integration = this.getIntegrationClone();
        const tags = integration.tags || [];
        const connectorIds = this.getSubsequentConnections(0).map(
          step => (step.connection ? step.connection.connectorId : undefined)
        );
        connectorIds.forEach(id => {
          if (id && tags.indexOf(id) === -1) {
            tags.push(id);
          }
        });
        const finishUp = (i: Integration, subscription: Subscription) => {
          log.debugc(
            () => 'Saved integration: ' + JSON.stringify(i, undefined, 2),
            category
          );
          const action = event['action'];
          if (action && typeof action === 'function') {
            action(i);
          }
          sub.unsubscribe();
        };
        integration.tags = tags;
        const sub = this.integrationStore.updateOrCreate(integration).subscribe(
          (i: Integration) => {
            if (!this._integration.id) {
              this._integration.id = i.id;
            }
            if (event.publish) {
              this.integrationSupportService
                .deploy(i)
                .toPromise()
                .then(() => {
                  finishUp(i, sub);
                });
            } else {
              finishUp(i, sub);
            }
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

  private isUserDefined(dataShape: DataShape) {
    return (
      dataShape &&
      dataShape.metadata &&
      dataShape.metadata.userDefined === 'true'
    );
  }

  private hasDataShape(step: Step, isInput = false): boolean {
    if (!step.action || !step.action.descriptor) {
      return false;
    }
    const descriptor = step.action.descriptor;
    const dataShape = isInput
      ? descriptor.inputDataShape
      : descriptor.outputDataShape;
    return dataShape && dataShape.kind !== DataShapeKinds.NONE;
  }

  private maybeDoAction(thing: any) {
    if (thing && typeof thing === 'function') {
      thing.call(thing);
    }
  }

  private insertStepBefore(position: number, stepKind?: string) {
    const stepConfig = this.stepStore.getDefaultStepDefinition(stepKind);
    const step = { ...createStep(), stepKind, ...stepConfig };
    this.steps.splice(position, 0, step);
  }

  private insertStepAfter(position: number, stepKind?: string) {
    const target = position + 1;
    const stepConfig = this.stepStore.getDefaultStepDefinition(stepKind);
    const step = { ...createStep(), stepKind, ...stepConfig };
    this.steps.splice(target, 0, step);
  }

  private insertConnectionAfter(position: number) {
    const target = position + 1;
    const step = createConnectionStep();
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
