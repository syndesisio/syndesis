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
        if (summary.errorCode && summary.errors) {
          throw new Error(
            summary.userMsg ||
              (summary.errors || [])
                .map((e: any) => e.message)
                .filter((m: string) => m)
                .join('\n')
          );
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
