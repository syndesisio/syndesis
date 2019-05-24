import { Connector } from '@syndesis/models';
import produce from 'immer';
import * as React from 'react';
import { ApiContext, IApiContext } from './ApiContext';
import { callFetch } from './callFetch';

export interface IWithApiConnectorHelpersChildrenProps {
  deleteApiConnector(apiConnectorId: string): Promise<void>;
  saveApiConnector(apiConnector: Connector): Promise<Connector>;
  updateApiConnector(
    apiConnector: Connector,
    name?: string,
    description?: string,
    host?: string,
    basePath?: string,
    icon?: string
  ): Connector;
}

export interface IWithApiConnectorHelpersProps {
  children(props: IWithApiConnectorHelpersChildrenProps): any;
}

export class WithApiConnectorHelpersWrapped extends React.Component<
  IWithApiConnectorHelpersProps & IApiContext
> {
  constructor(props: IWithApiConnectorHelpersProps & IApiContext) {
    super(props);
    this.deleteApiConnector = this.deleteApiConnector.bind(this);
    this.saveApiConnector = this.saveApiConnector.bind(this);
    this.updateApiConnector = this.updateApiConnector.bind(this);
  }

  /**
   * Deletes an API client connector.
   * @param apiConnectorId the ID of the API client connector being deleted
   */
  public async deleteApiConnector(apiConnectorId: string): Promise<void> {
    const response = await callFetch({
      headers: this.props.headers,
      method: 'DELETE',
      url: `${this.props.apiUri}/connectors/${apiConnectorId}`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  }

  /**
   * Saves an API client connector.
   * @param apiConnector the API client connector being saved
   * @returns the persisted connector
   */
  public async saveApiConnector(apiConnector: Connector): Promise<Connector> {
    const response = await callFetch({
      body: apiConnector,
      headers: this.props.headers,
      method: apiConnector.id ? 'PUT' : 'POST',
      url: apiConnector.id
        ? `${this.props.apiUri}/connectors/${apiConnector.id}`
        : `${this.props.apiUri}/connectors`,
    });
    if (!response.ok) {
      throw new Error(response.statusText);
    }
    return !apiConnector.id
      ? ((await response.json()) as Connector)
      : Promise.resolve(apiConnector);
  }

  public updateApiConnector(
    apiConnector: Connector,
    newName: string,
    newDescription?: string,
    newHost?: string,
    newBasePath?: string,
    newIcon?: string
  ): Connector {
    return produce(apiConnector, draft => {
      draft.name = newName;
      draft.description = newDescription;

      if (newIcon) {
        draft.icon = newIcon;
      }

      if (draft.configuredProperties) {
        if (newHost) {
          draft.configuredProperties.host = newHost;
        } else if (draft.configuredProperties.host) {
          const { host, ...noHost } = draft.configuredProperties;
          draft.configuredProperties = noHost;
        }

        if (newBasePath) {
          draft.configuredProperties.basePath = newBasePath;
        } else if (draft.configuredProperties.basePath) {
          const { basePath, ...noBasePath } = draft.configuredProperties;
          draft.configuredProperties = noBasePath;
        }
      } else if (newHost || newBasePath) {
        if (newHost) {
          draft.configuredProperties = {
            host: newHost,
          };
        }

        if (newBasePath) {
          if (draft.configuredProperties) {
            draft.configuredProperties.basePath = newBasePath;
          } else {
            draft.configuredProperties = {
              basePath: newBasePath,
            };
          }
        }
      }
    });
  }

  public render() {
    return this.props.children({
      deleteApiConnector: this.deleteApiConnector,
      saveApiConnector: this.saveApiConnector,
      updateApiConnector: this.updateApiConnector,
    });
  }
}

export const WithApiConnectorHelpers: React.FunctionComponent<
  IWithApiConnectorHelpersProps
> = props => (
  <ApiContext.Consumer>
    {apiContext => (
      <WithApiConnectorHelpersWrapped {...props} {...apiContext} />
    )}
  </ApiContext.Consumer>
);
