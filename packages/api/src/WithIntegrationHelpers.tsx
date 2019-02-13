import {
  Action,
  ActionDescriptor,
  Connection,
  Integration,
  IntegrationOverview,
  Step,
} from '@syndesis/models';
import { key } from '@syndesis/utils';
import produce from 'immer';
import * as React from 'react';
import { ApiContext, IApiContext } from './ApiContext';
import { callFetch } from './callFetch';

export const NEW_INTEGRATION = {
  name: '',
  tags: [],
} as Integration;

type UpdateOrAddConnection = (
  integration: Integration,
  connection: Connection,
  action: Action,
  flow: number,
  position: number,
  configuredProperties: any
) => Promise<Integration>;

export interface IWithIntegrationHelpersChildrenProps {
  /**
   * adds a step of type connection to the provided integration object.
   *
   * @param integration - the integration object to modify
   * @param connection - the connection object that's been used to set up the
   * step
   * @param action - the action that's been used to set up the step
   * @param flow - the zero-based index of the flow where to add the step
   * @param position - the zero-based index of the steps where to add the step
   * @param configuredProperties - the values configured by the user for the step
   *
   * @todo should we check `flow` and `position` to see if they are valid?
   * @todo perhaps rename it with a better name
   */
  addConnection: UpdateOrAddConnection;
  /**
   * updates a step of type connection to the provided integration object.
   *
   * @param integration - the integration object to modify
   * @param connection - the connection object that's been used to set up the
   * step
   * @param action - the action that's been used to set up the step
   * @param flow - the zero-based index of the flow where to add the step
   * @param position - the zero-based index of the steps where to add the step
   * @param configuredProperties - the values configured by the user for the step
   *
   * @todo perhaps rename it with a better name
   * @todo should we check `flow` and `position` to see if they are valid?
   */
  updateConnection: UpdateOrAddConnection;
  /**
   * updates a step of type connection to the provided integration object if
   * a step exists at the provided `flow` and `position` indexes; a new step is
   * added otherwise.
   *
   * @param integration - the integration object to modify
   * @param connection - the connection object that's been used to set up the
   * step
   * @param action - the action that's been used to set up the step
   * @param flow - the zero-based index of the flow where to add the step
   * @param position - the zero-based index of the steps where to add the step
   * @param configuredProperties - the values configured by the user for the step
   *
   * @todo perhaps rename it with a better name
   * @todo should we check `flow` and `position` to see if they are valid?
   */
  updateOrAddConnection: UpdateOrAddConnection;
  /**
   * returns an empty integration object.
   *
   * @todo make the returned object immutable to avoid uncontrolled changes
   */
  getEmptyIntegration(): Integration;
  /**
   * asynchronously saves the provided integration, returning the saved
   * integration in case of success.
   *
   * @param integration
   *
   * @todo make the returned object immutable to avoid uncontrolled changes
   */
  saveIntegration(integration: Integration): Promise<Integration>;
  /**
   * updates the name of an integration.
   *
   * @param integration
   * @param name
   */
  setName(integration: Integration, name: string): Integration;
  /**
   * returns the list of steps of the provided integration.
   *
   * @param value
   * @param flow
   *
   * @todo make the returned object immutable to avoid uncontrolled changes
   */
  getSteps(integration: Integration, flow: number): Step[];
  /**
   * returns a specific step of the provided integration.
   *
   * @param value
   * @param flow
   *
   * @todo make the returned object immutable to avoid uncontrolled changes
   */
  getStep(integration: Integration, flow: number, step: number): Step;
  /**
   * returns true if the provided integration can be published; returns false
   * otherwise.
   *
   * @param integration
   */
  canPublish(integration: IntegrationOverview): boolean;
  /**
   * returns true if the provided integration can be activated; returns false
   * otherwise.
   *
   * @param integration
   */
  canActivate(integration: IntegrationOverview): boolean;
  /**
   * returns true if the provided integration can be edited; returns false
   * otherwise.
   *
   * @param integration
   */
  canEdit(integration: IntegrationOverview): boolean;
  /**
   * returns true if the provided integration can be deactivated; returns false
   * otherwise.
   *
   * @param integration
   */
  canDeactivate(integration: IntegrationOverview): boolean;
}

export interface IWithIntegrationHelpersProps {
  children(props: IWithIntegrationHelpersChildrenProps): any;
}

export class WithIntegrationHelpersWrapped extends React.Component<
  IWithIntegrationHelpersProps & IApiContext
> {
  constructor(props: IWithIntegrationHelpersProps & IApiContext) {
    super(props);
    this.addConnection = this.addConnection.bind(this);
    this.getEmptyIntegration = this.getEmptyIntegration.bind(this);
    this.saveIntegration = this.saveIntegration.bind(this);
    this.setName = this.setName.bind(this);
    this.updateConnection = this.updateConnection.bind(this);
    this.updateOrAddConnection = this.updateOrAddConnection.bind(this);
    this.getStep = this.getStep.bind(this);
    this.getSteps = this.getSteps.bind(this);
  }

  public async getActionDescriptor(
    connectionId: string,
    actionId: string,
    configuredProperties: any
  ): Promise<ActionDescriptor | null> {
    if (configuredProperties) {
      const response = await callFetch({
        body: configuredProperties,
        headers: this.props.headers,
        method: 'POST',
        url: `${
          this.props.apiUri
        }/connections/${connectionId}/actions/${actionId}`,
      });
      if (!response.ok) {
        throw new Error(response.statusText);
      }
      return (await response.json()) as ActionDescriptor;
    } else {
      return null;
    }
  }

  public async addConnection(
    integration: Integration,
    connection: Connection,
    action: Action,
    flow: number,
    position: number,
    configuredProperties: any
  ): Promise<Integration> {
    const actionDescriptor = await this.getActionDescriptor(
      connection.id!,
      action.id!,
      configuredProperties
    );
    return produce(integration, draft => {
      if (!draft.flows) {
        draft.flows = [];
      }
      if (!draft.flows[flow]) {
        draft.flows[flow] = {
          id: key(),
          name: '',
          steps: [],
        };
      }
      if (!draft.flows[flow].steps) {
        draft.flows[flow].steps = [];
      }
      const step: Step = {
        action,
        configuredProperties,
        connection,
        id: draft.flows[flow].id,
      };
      if (actionDescriptor) {
        step.action!.descriptor = actionDescriptor;
      }
      step.stepKind = 'endpoint';
      draft.flows[flow].steps!.splice(position, 0, step);
      draft.tags = Array.from(new Set([...(draft.tags || []), connection.id!]));
    });
  }

  public async updateConnection(
    integration: Integration,
    connection: Connection,
    action: Action,
    flow: number,
    position: number,
    configuredProperties: any
  ): Promise<Integration> {
    const actionDescriptor = await this.getActionDescriptor(
      connection.id!,
      action.id!,
      configuredProperties
    );
    return produce(integration, draft => {
      if (!draft.flows) {
        draft.flows = [];
      }
      if (!draft.flows[flow]) {
        draft.flows[flow] = {
          id: key(),
          name: '',
          steps: [],
        };
      }
      if (!draft.flows[flow].steps) {
        draft.flows[flow].steps = [];
      }
      const step: Step = {
        action,
        configuredProperties,
        connection,
        id: draft.flows[flow].id,
      };
      if (actionDescriptor) {
        step.action!.descriptor = actionDescriptor;
      }
      step.stepKind = 'endpoint';
      draft.flows[flow].steps![position] = step;
    });
  }
  public async updateOrAddConnection(
    integration: Integration,
    connection: Connection,
    action: Action,
    flow: number,
    position: number,
    configuredProperties: any
  ): Promise<Integration> {
    const actionDescriptor = await this.getActionDescriptor(
      connection.id!,
      action.id!,
      configuredProperties
    );
    return produce(integration, draft => {
      if (!draft.flows) {
        draft.flows = [];
      }
      if (!draft.flows[flow]) {
        draft.flows[flow] = {
          id: key(),
          name: '',
          steps: [],
        };
      }
      if (!draft.flows[flow].steps) {
        draft.flows[flow].steps = [];
      }
      const step: Step = {
        action,
        configuredProperties,
        connection,
        id: draft.flows[flow].id,
      };
      if (actionDescriptor) {
        step.action!.descriptor = actionDescriptor;
      }
      step.stepKind = 'endpoint';
      if (draft.flows[flow].steps![position]) {
        draft.flows[flow].steps![position] = step;
      } else {
        draft.flows[flow].steps!.splice(position, 0, step);
        draft.tags = Array.from(
          new Set([...(draft.tags || []), connection.id!])
        );
      }
    });
  }

  public getEmptyIntegration(): Integration {
    return NEW_INTEGRATION;
  }

  public async saveIntegration(integration: Integration): Promise<Integration> {
    const response = await callFetch({
      body: integration,
      headers: this.props.headers,
      method: integration.id ? 'PUT' : 'POST',
      url: integration.id
        ? `${this.props.apiUri}/integrations/${integration.id}`
        : `${this.props.apiUri}/integrations`,
    });
    if (!response.ok) {
      throw new Error(response.statusText);
    }
    return !integration.id
      ? ((await response.json()) as Integration)
      : Promise.resolve(integration);
  }

  public setName(integration: Integration, name: string): Integration {
    return produce(integration, nextIntegration => {
      nextIntegration.name = name;
    });
  }

  public getSteps(integration: Integration, flow: number): Step[] {
    try {
      return integration.flows![flow].steps!;
    } catch (e) {
      throw new Error(`Can't find steps in position flow:${flow}`);
    }
  }

  public getStep(integration: Integration, flow: number, step: number): Step {
    try {
      return integration.flows![flow].steps![step];
    } catch (e) {
      throw new Error(
        `Can't find a step in position flow:${flow} step:${step}`
      );
    }
  }

  public canPublish(integration: IntegrationOverview) {
    return integration.currentState !== 'Pending';
  }

  public canActivate(integration: IntegrationOverview) {
    return (
      integration.currentState !== 'Pending' &&
      integration.currentState !== 'Published'
    );
  }

  public canEdit(integration: IntegrationOverview) {
    return integration.currentState !== 'Pending';
  }

  public canDeactivate(integration: IntegrationOverview) {
    return integration.currentState !== 'Unpublished';
  }

  public render() {
    return this.props.children({
      addConnection: this.addConnection,
      canActivate: this.canActivate,
      canDeactivate: this.canDeactivate,
      canEdit: this.canEdit,
      canPublish: this.canPublish,
      getEmptyIntegration: this.getEmptyIntegration,
      getStep: this.getStep,
      getSteps: this.getSteps,
      saveIntegration: this.saveIntegration,
      setName: this.setName,
      updateConnection: this.updateConnection,
      updateOrAddConnection: this.updateOrAddConnection,
    });
  }
}

/**
 * This component provides provides through a render propr a number of helper
 * functions useful when working with an integration.
 *
 * Some of these helpers are available also as stand-alone functions
 * (packages/api/src/integrationHelpers/index.ts), but this component provides
 * methods like `saveIntegration` that can talk with the backend without any
 * additional information provided.
 *
 * Methods that modify an integration return a immutable copy of the original
 * object, to reduce the risk of bugs.
 *
 * @see [addConnection]{@link IWithIntegrationHelpersChildrenProps#addConnection}
 * @see [updateConnection]{@link IWithIntegrationHelpersChildrenProps#updateConnection}
 * @see [updateOrAddConnection]{@link IWithIntegrationHelpersChildrenProps#updateOrAddConnection}
 * @see [getEmptyIntegration]{@link IWithIntegrationHelpersChildrenProps#getEmptyIntegration}
 * @see [saveIntegration]{@link IWithIntegrationHelpersChildrenProps#saveIntegration}
 * @see [setName]{@link IWithIntegrationHelpersChildrenProps#setName}
 * @see [getSteps]{@link IWithIntegrationHelpersChildrenProps#getSteps}
 * @see [getStep]{@link IWithIntegrationHelpersChildrenProps#getStep}
 * @see [canPublish]{@link IWithIntegrationHelpersChildrenProps#canPublish}
 * @see [canActivate]{@link IWithIntegrationHelpersChildrenProps#canActivate}
 * @see [canEdit]{@link IWithIntegrationHelpersChildrenProps#canEdit}
 * @see [canDeactivate]{@link IWithIntegrationHelpersChildrenProps#canDeactivate}
 */
export const WithIntegrationHelpers: React.FunctionComponent<
  IWithIntegrationHelpersProps
> = props => (
  <ApiContext.Consumer>
    {apiContext => <WithIntegrationHelpersWrapped {...props} {...apiContext} />}
  </ApiContext.Consumer>
);
