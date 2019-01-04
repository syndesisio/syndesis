import {
  Action,
  ActionDescriptor,
  Connection,
  Integration,
  Step,
} from '@syndesis/models';
import produce from 'immer';
import * as React from 'react';
import { ApiContext, IApiContext } from './ApiContext';
import { callFetch } from './callFetch';
import { key } from './helpers';

type AddConnection = (
  integration: Integration,
  connection: Connection,
  action: Action,
  flow: number,
  position: number,
  configuredProperties: {
    [name: string]: string;
  }
) => Promise<Integration>;
type GetEmptyIntegration = (integration?: Integration) => Integration;
type SaveIntegration = (integration: Integration) => Promise<Integration>;
type SetName = (integration: Integration, name: string) => Integration;

export interface IWithIntegrationHelpersChildrenProps {
  addConnection: AddConnection;
  getEmptyIntegration: GetEmptyIntegration;
  saveIntegration: SaveIntegration;
  setName: SetName;
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
  }

  public async getActionDescriptor(
    connectionId: string,
    actionId: string,
    configuredProperties: any
  ): Promise<ActionDescriptor> {
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
  }

  public async addConnection(
    integration: Integration,
    connection: Connection,
    action: Action,
    flow: number,
    position: number,
    configuredProperties: {
      [name: string]: string;
    }
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
      step.action!.descriptor = actionDescriptor;
      step.stepKind = 'endpoint';
      draft.flows[flow].steps!.splice(position, 0, step);
      draft.tags = Array.from(new Set([...(draft.tags || []), connection.id!]));
    });
  }

  public getEmptyIntegration(): Integration {
    return {
      name: '',
      tags: [],
    };
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

  public render() {
    return this.props.children({
      addConnection: this.addConnection,
      getEmptyIntegration: this.getEmptyIntegration,
      saveIntegration: this.saveIntegration,
      setName: this.setName,
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
