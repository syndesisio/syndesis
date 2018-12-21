import {
  Action,
  ConfigurationProperty,
  ConnectionOverview,
  Connector,
} from '@syndesis/models';
import { ActionDescriptor, ActionDescriptorStep } from '@syndesis/models/src';
import * as React from 'react';
import { IRestState } from './Rest';
import { SyndesisRest } from './SyndesisRest';

export function getActionsWithFrom(actions: Action[] = []) {
  return actions.filter(a => a.pattern === 'From');
}

export function getActionsWithTo(actions: Action[] = []) {
  return actions.filter(a => a.pattern === 'To');
}

export function getConnectionConnector(
  connection: ConnectionOverview
): Connector {
  if (!connection.connector) {
    throw Error(`FATAL: Connection ${connection.id} doesn't have a connector`);
  }
  return connection.connector;
}

export function getConnectorActions(connector: Connector): Action[] {
  return connector.actions;
}

export function getActionById(actions: Action[], actionId: string): Action {
  const action = actions.find(a => a.id === actionId);
  if (!action) {
    throw Error(`FATAL: Action ${actionId} not found`);
  }
  return action;
}

export function getActionDescriptor(action: Action): ActionDescriptor {
  if (!action.descriptor) {
    throw Error(`FATAL: Action ${action.id} doesn't have any descriptor`);
  }
  return action.descriptor;
}

export function getActionSteps(
  descriptor: ActionDescriptor
): ActionDescriptorStep[] {
  if (!descriptor.propertyDefinitionSteps) {
    throw Error(`FATAL: Descriptor doesn't have any definition`);
  }
  return descriptor.propertyDefinitionSteps;
}

export function getActionStep(
  steps: ActionDescriptorStep[],
  step: number
): ActionDescriptorStep {
  if (step > steps.length - 1) {
    throw Error(`FATAL: Step ${step} does not exist in the descriptor steps`);
  }
  return steps[step];
}

export function getActionStepDefinition(
  step: ActionDescriptorStep
): {
  [name: string]: ConfigurationProperty;
} {
  if (!step.properties) {
    throw Error(`FATAL: Step ${step} does not have valid properties`);
  }
  return step.properties;
}

export interface IConnectionResponse extends ConnectionOverview {
  readonly actionsWithFrom: Action[];
  readonly actionsWithTo: Action[];
  getActionById(actionId: string): Action;
  getActionStep(action: Action, step: number): ActionDescriptorStep;
  getActionStepDefinition(
    step: ActionDescriptorStep
  ): {
    [name: string]: ConfigurationProperty;
  };
  getActionSteps(action: Action): ActionDescriptorStep[];
}

export interface IWithConnectionProps {
  id: string;
  children(props: IRestState<IConnectionResponse>): any;
}

export class WithConnection extends React.Component<IWithConnectionProps> {
  public render() {
    return (
      <SyndesisRest<ConnectionOverview>
        url={`/connections/${this.props.id}`}
        defaultValue={{
          name: '',
        }}
      >
        {({ read, response }) =>
          this.props.children({
            ...response,
            data: {
              ...response.data,
              actionsWithFrom: getActionsWithFrom(
                response.data.connector ? response.data.connector.actions : []
              ),
              actionsWithTo: getActionsWithTo(
                response.data.connector ? response.data.connector.actions : []
              ),
              getActionById: (actionId: string) =>
                getActionById(
                  getConnectorActions(getConnectionConnector(response.data)),
                  actionId
                ),
              getActionStep: (action: Action, step: number) =>
                getActionStep(
                  getActionSteps(getActionDescriptor(action)),
                  step
                ),
              getActionStepDefinition: (step: ActionDescriptorStep) =>
                getActionStepDefinition(step),
              getActionSteps: (action: Action) =>
                getActionSteps(getActionDescriptor(action)),
            },
          })
        }
      </SyndesisRest>
    );
  }
}
