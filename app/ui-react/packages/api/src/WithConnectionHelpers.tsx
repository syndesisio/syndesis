import { Connection, Connector } from '@syndesis/models';
import produce from 'immer';
import * as React from 'react';
import { ApiContext, IApiContext } from './ApiContext';
import { callFetch } from './callFetch';

export interface IConfigurationValidationViolation {
  code: string;
  description: string;
  parameters: string[];
  attributes: { [key: string]: string };
}

export interface IConfigurationValidation {
  status: 'OK' | 'ERROR';
  scope: 'PARAMETERS' | 'CONNECTIVITY';
  errors?: IConfigurationValidationViolation[];
}

export interface IValidationResult {
  isError: boolean;
  error?: string;
  message?: string;
  property?: string;
}

export interface IWithConnectionHelpersChildrenProps {
  /**
   * asynchronously saves the provided connection, returning the saved
   * connection in case of success.
   *
   * @param connection
   *
   * @todo make the returned object immutable to avoid uncontrolled changes
   */
  validateConfiguration(
    connectionId: string,
    values: { [key: string]: string }
  ): Promise<IConfigurationValidation[]>;
  /**
   * create a new Connection object starting from a Connector.
   *
   * @param connector
   * @param name
   * @param description
   */
  createConnection(
    connector: Connector,
    name: string,
    description: string,
    configuredProperties: { [key: string]: string }
  ): Connection;
  /**
   * asynchronously saves the provided connection, returning the saved
   * connection in case of success.
   *
   * @param connection
   *
   * @todo make the returned object immutable to avoid uncontrolled changes
   */
  saveConnection(connection: Connection): Promise<Connection>;
  /**
   * create a new Connection object starting from an existing Connection.
   *
   * @param connection
   * @param name
   * @param description
   * @param configuredProperties
   */
  updateConnection(
    connection: Connection,
    name?: string,
    description?: string,
    configuredProperties?: { [key: string]: string }
  ): Connection;
  /**
   * asynchronously deletes the provided connection.
   *
   * @param connectionId the connection ID
   */
  deleteConnection(connectionId: string): Promise<void>;
  /**
   *  Asynchronously validates the proposed connection name.
   *
   * @param connection the connection whose name is being changed
   * @param proposedName the name being validated
   * @returns a validation result
   */
  validateName(
    connection: Connection,
    proposedName: string
  ): Promise<IValidationResult>;
}

export interface IWithConnectionHelpersProps {
  children(props: IWithConnectionHelpersChildrenProps): any;
}

export class WithConnectionHelpersWrapped extends React.Component<
  IWithConnectionHelpersProps & IApiContext
> {
  constructor(props: IWithConnectionHelpersProps & IApiContext) {
    super(props);
    this.createConnection = this.createConnection.bind(this);
    this.deleteConnection = this.deleteConnection.bind(this);
    this.updateConnection = this.updateConnection.bind(this);
    this.validateConfiguration = this.validateConfiguration.bind(this);
    this.saveConnection = this.saveConnection.bind(this);
    this.validateName = this.validateName.bind(this);
  }

  public createConnection(
    connector: Connector,
    name: string,
    description: string,
    configuredProperties: { [key: string]: string }
  ): Connection {
    const connection = {} as Connection;
    return produce(connection, draft => {
      connection.name = name;
      connection.description = description;
      connection.configuredProperties = configuredProperties;
      connection.connector = connector;
      connection.connectorId = connector.id;
      connection.icon = connector.icon;
    });
  }

  public async deleteConnection(connectionId: string): Promise<void> {
    const response = await callFetch({
      headers: this.props.headers,
      method: 'DELETE',
      url: `${this.props.apiUri}/connections/${connectionId}`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  }

  public updateConnection(
    connection: Connection,
    name?: string,
    description?: string,
    configuredProperties?: { [key: string]: string }
  ): Connection {
    return produce(connection, draft => {
      connection.name = name || connection.name;
      // allow empty descriptions
      connection.description =
        description === undefined ? connection.description : description;
      connection.configuredProperties =
        configuredProperties || connection.configuredProperties;
    });
  }

  public async validateConfiguration(
    connectorId: string,
    values: { [key: string]: string }
  ): Promise<IConfigurationValidation[]> {
    const response = await callFetch({
      body: values,
      headers: this.props.headers,
      method: 'POST',
      url: `${this.props.apiUri}/connectors/${connectorId}/verifier`,
    });
    if (!response.ok) {
      throw new Error(response.statusText);
    }
    return (await response.json()) as IConfigurationValidation[];
  }

  public async saveConnection(connection: Connection): Promise<Connection> {
    const response = await callFetch({
      body: connection,
      headers: this.props.headers,
      method: connection.id ? 'PUT' : 'POST',
      url: connection.id
        ? `${this.props.apiUri}/connections/${connection.id}`
        : `${this.props.apiUri}/connections`,
    });
    if (!response.ok) {
      throw new Error(response.statusText);
    }
    return !connection.id
      ? ((await response.json()) as Connection)
      : Promise.resolve(connection);
  }

  public async validateName(
    connection: Connection,
    proposedName: string
  ): Promise<IValidationResult> {
    // short circuit if name has not changed
    if (connection.name === proposedName) {
      return {
        isError: false,
      };
    }

    const testConn = { name: proposedName };
    const response = await callFetch({
      body: testConn,
      headers: this.props.headers,
      method: 'POST',
      url: `${this.props.apiUri}/connections/validation`,
    });

    if (response.ok) {
      return {
        isError: false,
      };
    }

    // return the first error
    const result = await response.json();
    return {
      isError: true,
      ...result[0],
    };
  }

  public render() {
    return this.props.children({
      createConnection: this.createConnection,
      deleteConnection: this.deleteConnection,
      saveConnection: this.saveConnection,
      updateConnection: this.updateConnection,
      validateConfiguration: this.validateConfiguration,
      validateName: this.validateName,
    });
  }
}

/**
 * This component provides provides through a render propr a number of helper
 * functions useful when working with a connection.
 *
 * Methods that modify a connection return a immutable copy of the original
 * object, to reduce the risk of bugs.
 *
 * @see [saveConnection]{@link IWithConnectionHelpersChildrenProps#saveConnection}
 */
export const WithConnectionHelpers: React.FunctionComponent<
  IWithConnectionHelpersProps
> = props => (
  <ApiContext.Consumer>
    {apiContext => <WithConnectionHelpersWrapped {...props} {...apiContext} />}
  </ApiContext.Consumer>
);
