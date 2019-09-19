import { APISummary } from '@syndesis/models';
import * as React from 'react';
import { ApiContext } from './ApiContext';
import { callFetch } from './callFetch';

export function useApiProviderSummary(specification: string) {
  const apiContext = React.useContext(ApiContext);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState<false | Error>(false);
  const [apiSummary, setApiSummary] = React.useState<APISummary | undefined>(
    undefined
  );

  React.useEffect(() => {
    const fetchSummary = async () => {
      setLoading(true);
      try {
        const body = new FormData();
        body.append('specification', specification);
        const response = await callFetch({
          body,
          headers: apiContext.headers,
          includeAccept: true,
          includeContentType: false,
          method: 'POST',
          url: `${apiContext.apiUri}/apis/info`,
        });
        const summary = await response.json();
        if (summary.errorCode) {
          throw new Error(summary.userMsg);
        }
        if (!summary.actionsSummary) {
          let errorMessage = '';
          // we should be getting an array of error objects
          if (Array.isArray(summary.errors)) {
            errorMessage = summary.errors
              .map((e: string | any) => (e.message ? e.message : e))
              .join('\n');
          } else {
            // but in case we don't, let's show what we got and hope for the best
            errorMessage = JSON.stringify(summary);
          }
          throw new Error(errorMessage);
        }
        setApiSummary(summary as APISummary);
      } catch (e) {
        setError(e as Error);
      } finally {
        setLoading(false);
      }
    };
    fetchSummary();
  }, [specification, apiContext, setLoading, setApiSummary, setError]);

  return { apiSummary, loading, error };
}
