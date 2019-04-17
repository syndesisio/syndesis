import {
  Action,
  ActionDescriptor,
  Connection,
  Integration,
  Step,
} from '@syndesis/models';
import { key } from '@syndesis/utils';
import { saveAs } from 'file-saver';
import produce from 'immer';
import * as React from 'react';
import { ApiContext, IApiContext } from './ApiContext';
import { callFetch } from './callFetch';
import { PUBLISHED, UNPUBLISHED } from './constants';

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
   * Delete the integration with the specified ID, empty response is returned
   * @param id
   */
  deleteIntegration(id: string): Promise<Response>;
  /**
   * Deploy the integration with the specified ID and version.  Empty response is returned
   *
   * @param id
   * @param version
   * @param isIntegrationDeployment
   */
  deployIntegration(
    id: string,
    version: string | number,
    isIntegrationDeployment?: boolean
  ): Promise<Response>;
  /**
   * Requests a .zip file of the integration, using the specified filename
   * @param id
   * @param fileName
   */
  exportIntegration(id: string, fileName: string): Promise<void>;
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
   * Request that the given integration ID at the given version be deactivated, empty response is returned
   * @param id
   * @param version
   */

  undeployIntegration(id: string, version: string | number): Promise<Response>;
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
    this.deleteIntegration = this.deleteIntegration.bind(this);
    this.deployIntegration = this.deployIntegration.bind(this);
    this.exportIntegration = this.exportIntegration.bind(this);
    this.saveIntegration = this.saveIntegration.bind(this);
    this.undeployIntegration = this.undeployIntegration.bind(this);
    this.updateConnection = this.updateConnection.bind(this);
    this.updateOrAddConnection = this.updateOrAddConnection.bind(this);
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

  public async deleteIntegration(id: string) {
    return callFetch({
      headers: this.props.headers,
      method: 'DELETE',
      url: `${this.props.apiUri}/integrations/${id}`,
    });
  }

  public async exportIntegration(id: string, fileName: string) {
    return callFetch({
      headers: this.props.headers,
      method: 'GET',
      url: `${this.props.apiUri}/integration-support/export.zip?id=${id}`,
    }).then(async body => saveAs(await body.blob(), fileName));
  }

  public async deployIntegration(
    id: string,
    version: string | number,
    isIntegrationDeployment = false
  ) {
    return callFetch({
      body: isIntegrationDeployment ? { targetState: PUBLISHED } : {},
      headers: this.props.headers,
      method: isIntegrationDeployment ? 'POST' : 'PUT',
      url: isIntegrationDeployment
        ? `${
            this.props.apiUri
          }/integrations/${id}/deployments/${version}/targetState`
        : `${this.props.apiUri}/integrations/${id}/deployments`,
    });
  }

  public async undeployIntegration(id: string, version: string | number) {
    return callFetch({
      body: { targetState: UNPUBLISHED },
      headers: this.props.headers,
      method: 'POST',
      url: `${
        this.props.apiUri
      }/integrations/${id}/deployments/${version}/targetState`,
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

  public render() {
    return this.props.children({
      addConnection: this.addConnection,
      deleteIntegration: this.deleteIntegration,
      deployIntegration: this.deployIntegration,
      exportIntegration: this.exportIntegration,
      saveIntegration: this.saveIntegration,
      undeployIntegration: this.undeployIntegration,
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
 * @see [saveIntegration]{@link IWithIntegrationHelpersChildrenProps#saveIntegration}
 */
export const WithIntegrationHelpers: React.FunctionComponent<
  IWithIntegrationHelpersProps
> = props => (
  <ApiContext.Consumer>
    {apiContext => <WithIntegrationHelpersWrapped {...props} {...apiContext} />}
  </ApiContext.Consumer>
);
