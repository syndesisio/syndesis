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

type CreateIntegration = (integration?: Integration) => Integration;
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

export interface IWithIntegrationHelpersChildrenProps {
  createIntegration: CreateIntegration;
  addConnection: AddConnection;
}

export interface IWithIntegrationHelpersProps {
  children(props: IWithIntegrationHelpersChildrenProps): any;
}

export interface IWithIntegrationHelpersState {
  error: boolean;
  errorMessage?: string;
  loading: boolean;
}

export class WithIntegrationHelpersWrapped extends React.Component<
  IWithIntegrationHelpersProps & IApiContext,
  IWithIntegrationHelpersState
> {
  constructor(props: IWithIntegrationHelpersProps & IApiContext) {
    super(props);
    this.addConnection = this.addConnection.bind(this);
    this.createIntegration = this.createIntegration.bind(this);
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
    let nextIntegration: Integration = integration;
    try {
      this.setState({ loading: true });
      const actionDescriptor = await this.getActionDescriptor(
        connection.id!,
        action.id!,
        configuredProperties
      );
      nextIntegration = produce(integration, draft => {
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
        draft.flows[flow].steps!.splice(position, 0, step);
      });
    } catch (e) {
      this.setState({
        error: true,
        errorMessage: e.message,
      });
    } finally {
      this.setState({ loading: false });
    }
    return nextIntegration;
  }

  public createIntegration(): Integration {
    return {
      id: 'todo',
      name: '',
      tags: [],
    };
  }

  public render() {
    return this.props.children({
      addConnection: this.addConnection,
      createIntegration: this.createIntegration,
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
