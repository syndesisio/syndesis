import * as React from 'react';

import { IApiSummarySoap } from '@syndesis/models';
import { ApiContext } from './ApiContext';
import { callFetch } from './callFetch';

interface IApiConnectorSummaryOptions {
  portName?: string;
  serviceName?: string;
  wsdlUrl?: string;
  specification?: string;
}

export function useApiConnectorSummary(
  specification?: string,
  url?: string,
  connectorTemplateId?: string,
  /**
   * `configured` = an object that contains
   * optional properties we want to send to the
   * API when requesting an API connector summary.
   * i.e.: portName + serviceName for SOAP connector
   */
  configured?: IApiConnectorSummaryOptions
) {
  const apiContext = React.useContext(ApiContext);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState<false | Error>(false);
  const [apiSummary, setApiSummary] = React.useState<
    IApiSummarySoap | undefined
  >(undefined);

  React.useEffect(() => {
    if (!specification && !url) {
      // if there is no specification to send, either given by the
      // specification (raw upload) or pointed to by the url, we
      // don't invoke the server API to gather the summary
      return;
    }

    const fetchSummary = async () => {
      setLoading(true);

      const body = new FormData();
      const connectorSettings = {
        configuredProperties: {
          ...configured,
        } as IApiConnectorSummaryOptions,
        connectorTemplateId:
          connectorTemplateId ?? 'swagger-connector-template',
      };
      if (url) {
        connectorSettings.configuredProperties.specification = url;
      }

      body.append(
        'connectorSettings',
        new Blob([JSON.stringify(connectorSettings)], {
          type: 'application/json',
        })
      );
      if (specification) {
        body.append('specification', specification);
      }

      try {
        const response = await callFetch({
          body,
          headers: apiContext.headers,
          includeAccept: true,
          includeContentType: false,
          method: 'POST',
          url: `${apiContext.apiUri}/connectors/custom/info`,
        });
        const summary = await response.json();
        setApiSummary(summary as IApiSummarySoap);
      } catch (e) {
        setError(e as Error);
      } finally {
        setLoading(false);
      }
    };
    fetchSummary();
  }, [specification, apiContext, setLoading, setApiSummary, setError]);

  return { apiSummary, loading, error, setError };
}
