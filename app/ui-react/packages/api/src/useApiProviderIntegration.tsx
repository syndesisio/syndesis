import { Integration } from '@syndesis/models';
import * as React from 'react';
import { ApiContext } from './ApiContext';
import { callFetch } from './callFetch';
import { isIntegrationEmpty } from './helpers';

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
      throw new Error(integration.userMsg);
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
    if (response.status === 202) {
      const updatedIntegration = await response.json();
      if (updatedIntegration.errorCode) {
        throw new Error(updatedIntegration.userMsg);
      }
      return updatedIntegration;
    } else if (response.status === 304) {
      return integration;
    } else {
      throw new Error(
        `Unexpected return code ${response.status}: ${response.statusText}`
      );
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
