import { Connection, Connector } from '@syndesis/models';
import produce from 'immer';
import * as React from 'react';
import { ApiContext } from './ApiContext';
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

export const useConnectionHelpers = () => {
  const apiContext = React.useContext(ApiContext);

  const createConnection = (
    connector: Connector,
    name: string,
    description: string,
    configuredProperties?: { [key: string]: string }
  ): Connection => {
    const connection = {} as Connection;
    return produce(connection, draft => {
      draft.name = name;
      draft.description = description;
      draft.configuredProperties = configuredProperties;
      draft.connector = connector;
      draft.connectorId = connector.id;
      draft.icon = connector.icon;
    });
  };

  const deleteConnection = async (connectionId: string): Promise<void> => {
    const response = await callFetch({
      headers: apiContext.headers,
      method: 'DELETE',
      url: `${apiContext.apiUri}/connections/${connectionId}`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  };

  const updateConnection = (
    connection: Connection,
    name?: string,
    description?: string,
    configuredProperties?: { [key: string]: string }
  ): Connection => {
    return produce(connection, draft => {
      draft.name = name || draft.name;
      // allow empty descriptions
      draft.description =
        description === undefined ? draft.description : description;
      draft.configuredProperties =
        configuredProperties || draft.configuredProperties;
    });
  };

  const validateConfiguration = async (
    connectorId: string,
    values: { [key: string]: string }
  ): Promise<IConfigurationValidation[]> => {
    const response = await callFetch({
      body: values,
      headers: apiContext.headers,
      method: 'POST',
      url: `${apiContext.apiUri}/connectors/${connectorId}/verifier`,
    });
    if (!response.ok) {
      throw new Error(response.statusText);
    }
    return (await response.json()) as IConfigurationValidation[];
  };

  const saveConnection = async (
    connection: Connection
  ): Promise<Connection> => {
    return produce(connection, async draft => {
      const response = await callFetch({
        body: draft,
        headers: apiContext.headers,
        method: draft.id ? 'PUT' : 'POST',
        url: draft.id
          ? `${apiContext.apiUri}/connections/${draft.id}`
          : `${apiContext.apiUri}/connections`,
      });
      if (!response.ok) {
        throw new Error(response.statusText);
      }
      return !draft.id
        ? ((await response.json()) as Connection)
        : Promise.resolve(draft);
    });
  };

  const validateName = async (
    connection: Connection,
    proposedName: string
  ): Promise<IValidationResult> => {
    // short circuit if name has not changed
    if (connection.name === proposedName) {
      return {
        isError: false,
      };
    }

    const testConn = { name: proposedName };
    const response = await callFetch({
      body: testConn,
      headers: apiContext.headers,
      method: 'POST',
      url: `${apiContext.apiUri}/connections/validation`,
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
  };

  return {
    createConnection,
    deleteConnection,
    saveConnection,
    updateConnection,
    validateConfiguration,
    validateName,
  };
};
