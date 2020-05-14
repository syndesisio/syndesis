import * as React from 'react';
import { ApiContext } from './ApiContext';
import { callFetch } from './callFetch';

export interface ICreateConnectorProps {
  authenticationType?: string | undefined;
  authorizationEndpoint?: string;
  tokenEndpoint?: string;
  specification: string;
  name: string;
  description?: string;
  host?: string;
  basePath?: string;
  icon?: string;
}

export function useApiConnectorCreator() {
  const apiContext = React.useContext(ApiContext);

  const createConnector = async (connector: ICreateConnectorProps) => {
    const body = new FormData();
    // tslint:disable:no-console
    console.log('create connector..');
    body.append(
      'connectorSettings',
      new Blob(
        [
          JSON.stringify({
            configuredProperties: {
              authenticationType: connector.authenticationType,
              authorizationEndpoint: connector.authorizationEndpoint,
              basePath: connector.basePath,
              host: connector.host,
              specification: connector.specification,
              tokenEndpoint: connector.tokenEndpoint,
            },
            connectorTemplateId: 'swagger-connector-template',
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
    console.log(
      'integration from useApiConnectorCreator: ' + JSON.stringify(integration)
    );
    if (integration.errorCode) {
      throw new Error(integration.userMsg);
    }
    return integration;
  };

  return createConnector;
}
