import { ErrorResponse, Integration } from '@syndesis/models';
import * as React from 'react';
import { ApiContext } from './ApiContext';
import { callFetch } from './callFetch';
import { isIntegrationEmpty, throwStandardError } from './helpers';

export function useApiProviderIntegration() {
  const apiContext = React.useContext(ApiContext);

  const getNewIntegrationFromSpecification = async (specification: string) => {
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
      throw integration as ErrorResponse;
    }
    return integration;
  };

  const getUpdatedIntegrationFromSpecification = async (
    specification: string,
    integration: Integration
  ) => {
    const body = new FormData();
    body.append('specification', specification);
    body.append(
      'integration',
      new Blob([JSON.stringify(integration)], { type: 'application/json' })
    );
    const response = await callFetch({
      body,
      headers: apiContext.headers,
      includeAccept: true,
      includeContentType: false,
      method: 'PUT',
      url: `${apiContext.apiUri}/apis/generator`,
    });
    switch (response.status) {
      case 202:
        const updatedIntegration = await response.json();
        if (updatedIntegration.errorCode) {
          throw updatedIntegration as ErrorResponse;
        }
        return updatedIntegration;
      case 304:
        return integration;
      default:
        await throwStandardError(response);
    }
  };

  const getIntegration = async (
    specification: string,
    integration: Integration
  ) => {
    /**
     * Check if we are dealing with a new integration. If it's the case, we need to fetch a new integration object. Else,
     * we need to pass the current integration object to avoid losing any customization done in the meanwhile.
     */
    if (isIntegrationEmpty(integration)) {
      return getNewIntegrationFromSpecification(specification);
    } else {
      return getUpdatedIntegrationFromSpecification(specification, integration);
    }
  };

  return getIntegration;
}
