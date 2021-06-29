import * as React from 'react';

import { IApiSummarySoap } from '@syndesis/models';
import { ApiContext } from './ApiContext';
import { callFetch } from './callFetch';

interface IApiConnectorSummaryOptions {
  portName?: string;
  serviceName?: string;
  wsdlUrl?: string;
}

export function useApiConnectorSummary(
  specification: string,
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
    if (!specification) {
      return;
    }
    const fetchSummary = async () => {
      setLoading(true);

      try {
        const response = await callFetch({
          body: {
            configuredProperties: {
              ...configured,
              specification,
            },
            connectorTemplateId: connectorTemplateId
              ? connectorTemplateId
              : 'swagger-connector-template',
          },
          headers: apiContext.headers,
          includeAccept: true,
          includeContentType: true,
          method: 'POST',
          url: `${apiContext.apiUri}/connectors/custom/info`,
        });
        const summary = await response.json();
        if (summary.errorCode) {
          throw new Error(summary.userMsg);
        }
        if (connectorTemplateId === 'soap-connector-template' && Array.isArray(summary.errors) && summary.errors.length > 0) {
          const errorMessage = summary.errors
              .map((e: string | any) => (e.message ? e.message : e))
              .join('\n');
          throw new Error(errorMessage);
        }
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
