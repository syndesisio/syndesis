import * as React from 'react';
import { ApiContext } from './ApiContext';
import { callFetch, FetchMethod, IFetch } from './callFetch';
export interface IUseApiResource<T> {
  url: string;
  defaultValue: T;
  body?: any;
  method?: FetchMethod;
  initialValue?: T;
  transformResponse?: (response: Response) => Promise<T>;
  readOnMount?: boolean;
}
export function useApiResource<T>({
  body,
  url,
  defaultValue,
  method = 'GET',
  initialValue,
  transformResponse = async r => (await r.json()) as T,
  readOnMount = true,
}: IUseApiResource<T>) {
  const apiContext = React.useContext(ApiContext);
  const [loading, setLoading] = React.useState(readOnMount);
  const [hasData, setHasData] = React.useState(!!initialValue);
  const [error, setError] = React.useState<false | Error>(false);
  const [resource, setResource] = React.useState<T>(
    initialValue || defaultValue
  );

  const previousUrl = React.useRef<string>();

  async function fetchResource(
    {
      body: fBody,
      url: fUrl,
      headers: fHeaders,
      method: fMethod,
      ...props
    }: IFetch = { body, url, method }
  ) {
    let r: T | null = null;
    setLoading(true);
    try {
      const response = await callFetch({
        ...(props || {}),
        body: fBody,
        headers: {
          ...apiContext.headers,
          ...(fHeaders || {}),
        },
        method: fMethod,
        url: `${apiContext.apiUri}${fUrl}`,
      });
      if (!response.ok) {
        throw new Error(response.statusText);
      }
      r = await transformResponse(response);
      setResource(r);
      setHasData(true);
    } catch (e) {
      setHasData(false);
      setError(e);
    } finally {
      setLoading(false);
    }
    return r;
  }

  const read = React.useCallback(fetchResource, [
    callFetch,
    apiContext,
    url,
    setLoading,
    setResource,
    setError,
    setHasData,
    body,
  ]);

  React.useEffect(() => {
    if (previousUrl.current !== url && readOnMount) {
      read();
      previousUrl.current = url;
    }
  }, [url, previousUrl, read, readOnMount]);

  return { resource, loading, error, hasData, read };
}
