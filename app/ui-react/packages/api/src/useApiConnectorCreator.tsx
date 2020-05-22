import { ICreateConnectorProps } from '@syndesis/models';
import * as React from 'react';
import { ApiContext } from './ApiContext';
import { callFetch } from './callFetch';

export function useApiConnectorCreator() {
  const apiContext = React.useContext(ApiContext);

  const createConnector = async (connector: ICreateConnectorProps) => {
    const body = new FormData();

    body.append(
      'connectorSettings',
      new Blob(
        [
          JSON.stringify({
            configuredProperties: {
              addTimestamp: connector.addTimestamp,
              addUsernameTokenCreated: connector.addUsernameTokenCreated,
              addUsernameTokenNonce: connector.addUsernameTokenNonce,
              authenticationType: connector.authenticationType,
              authorizationEndpoint: connector.authorizationEndpoint,
              basePath: connector.basePath,
              host: connector.host,
              password: connector.password,
              passwordType: connector.passwordType,
              portName: connector.portName,
              serviceName: connector.serviceName,
              specification: connector.specification,
              tokenEndpoint: connector.tokenEndpoint,
              username: connector.username,
            },
            connectorTemplateId: connector.connectorTemplateId
              ? connector.connectorTemplateId
              : 'swagger-connector-template',
            description: connector.description,
            icon: connector.icon,
            name: connector.name,
          }),
        ],
        { type: 'application/json' }
      )
    );

    const response = await callFetch({
      body,
      headers: apiContext.headers,
      includeAccept: true,
      includeContentType: false,
      method: 'POST',
      url: `${apiContext.apiUri}/connectors/custom`,
    });

    const integration = await response.json();

    if (integration.errorCode) {
      throw new Error(integration.userMsg);
    }
    return integration;
  };

  return createConnector;
}
