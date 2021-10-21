import * as React from 'react';
import { ApiContext } from './ApiContext';
import { callFetch } from './callFetch';

/**
 * Customizable properties in API Client Connector wizard
 */
interface ICreateApiConnectorProps {
  connectorTemplateId: string;
  description: string;
  icon?: string;
  name: string;
  configuredProperties: {
    [name: string]: string;
  };
}

export function useApiConnectorCreator(specification?: string) {
  const apiContext = React.useContext(ApiContext);

  const createConnector = async (connector: ICreateApiConnectorProps) => {
    const body = new FormData();

    body.append(
      'connectorSettings',
      new Blob([JSON.stringify(connector)], { type: 'application/json' })
    );
    if (specification) {
      body.append('specification', specification);
    }

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
