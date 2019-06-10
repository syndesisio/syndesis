import * as React from 'react';
import { ApiContext } from './ApiContext';
import { callFetch } from './callFetch';

export function useApiProviderIntegration() {
  const apiContext = React.useContext(ApiContext);

  const getIntegration = async (specification: string) => {
    const body = new FormData();
    body.append('specification', specification);
    const response = await callFetch({
      body,
      headers: apiContext.headers,
      includeAccept: true,
      includeContentType: false,
      method: 'POST',
      url: `${apiContext.apiUri}/apis/generator`,
    });
    const integration = await response.json();
    if (integration.errorCode) {
      throw new Error(integration.userMsg);
    }
    return integration;
  };

  return getIntegration;
}
