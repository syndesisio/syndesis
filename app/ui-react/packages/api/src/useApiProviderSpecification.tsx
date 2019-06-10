import { Integration } from '@syndesis/models';
import * as React from 'react';
import { ApiContext } from './ApiContext';
import { callFetch } from './callFetch';

export function useApiProviderSpecification(
  specificationOrIntegration: string | Integration
) {
  const apiContext = React.useContext(ApiContext);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState<false | Error>(false);
  const [specification, setSpecification] = React.useState<string | undefined>(
    undefined
  );

  React.useEffect(() => {
    const fetchSpecification = async () => {
      setLoading(true);
      try {
        const response = await callFetch({
          headers: apiContext.headers,
          method: 'GET',
          url: `${apiContext.apiUri}/integrations/${
            (specificationOrIntegration as Integration).id
          }/specification`,
        });
        const integrationSpecification = await response.json();
        if (integrationSpecification.errorCode) {
          throw new Error(integrationSpecification.userMsg);
        }
        setSpecification(JSON.stringify(integrationSpecification));
      } catch (e) {
        setError(e as Error);
      } finally {
        setLoading(false);
      }
    };
    if (typeof specificationOrIntegration === 'string') {
      setSpecification(specificationOrIntegration);
    } else {
      fetchSpecification();
    }
  }, [specificationOrIntegration, apiContext, setLoading]);

  return { specification, loading, error };
}
