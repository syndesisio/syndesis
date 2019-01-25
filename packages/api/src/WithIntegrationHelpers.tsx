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
import {
  deserializeIntegration,
  serializeIntegration,
} from './integrationHelpers';

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
  addConnection: UpdateOrAddConnection;
  updateConnection: UpdateOrAddConnection;
  updateOrAddConnection: UpdateOrAddConnection;
  getEmptyIntegration(integration?: Integration): Integration;
  saveIntegration(integration: Integration): Promise<Integration>;
  setName(integration: Integration, name: string): Integration;
  createDraft(value: Integration): Promise<string>;
  getDraft(id: string): Promise<Integration>;
  getSteps(value: Integration, flow: number): Step[];
  getStep(value: Integration, flow: number, step: number): Step;
  setDraft(id: string, value: Integration): Promise<void>;
  canPublish(integration: IntegrationOverview): boolean;
  canActivate(integration: IntegrationOverview): boolean;
  canEdit(integration: IntegrationOverview): boolean;
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
    this.makeLocalStorageId = this.makeLocalStorageId.bind(this);
    this.createDraft = this.createDraft.bind(this);
    this.getDraft = this.getDraft.bind(this);
    this.setDraft = this.setDraft.bind(this);
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
    return (await response.json()) as Integration;
  }

  public setName(integration: Integration, name: string): Integration {
    return produce(integration, nextIntegration => {
      nextIntegration.name = name;
    });
  }

  public makeLocalStorageId(id: string) {
    return `iec-${id}`;
  }

  public async createDraft(value: Integration) {
    // TODO: this should be handled by the BE
    if (!value.id) {
      throw new Error("Integration has no id, can't create a draft");
    }
    const id = value.id;
    await this.setDraft(id, value);
    return Promise.resolve(id);
  }

  public getDraft(id: string): Promise<Integration> {
    // TODO: this should be handled by the BE
    const serializedIntegration = localStorage.getItem(
      this.makeLocalStorageId(id)
    );
    if (!serializedIntegration) {
      throw new Error(`There is no draft for id ${id}`);
    }
    return Promise.resolve(deserializeIntegration(serializedIntegration));
  }

  public setDraft(id: string, value: Integration): Promise<void> {
    // TODO: this should be handled by the BE
    localStorage.setItem(
      this.makeLocalStorageId(id),
      serializeIntegration(value)
    );
    return Promise.resolve();
  }

  public getSteps(value: Integration, flow: number): Step[] {
    try {
      return value.flows![flow].steps!;
    } catch (e) {
      throw new Error(`Can't find steps in position flow:${flow}`);
    }
  }

  public getStep(value: Integration, flow: number, step: number): Step {
    try {
      return value.flows![flow].steps![step];
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
      createDraft: this.createDraft,
      getDraft: this.getDraft,
      getEmptyIntegration: this.getEmptyIntegration,
      getStep: this.getStep,
      getSteps: this.getSteps,
      saveIntegration: this.saveIntegration,
      setDraft: this.setDraft,
      setName: this.setName,
      updateConnection: this.updateConnection,
      updateOrAddConnection: this.updateOrAddConnection,
    });
  }
}

export const WithIntegrationHelpers: React.FunctionComponent<
  IWithIntegrationHelpersProps
> = props => (
  <ApiContext.Consumer>
    {apiContext => <WithIntegrationHelpersWrapped {...props} {...apiContext} />}
  </ApiContext.Consumer>
);
