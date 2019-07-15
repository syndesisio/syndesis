import deepmerge from 'deepmerge';
import * as React from 'react';
import equal from 'react-fast-compare';
import { ApiContext } from './ApiContext';
import { callFetch, FetchMethod, IFetch } from './callFetch';
export interface IUseApiResource<T> {
  useDvApiUrl?: boolean;
  url: string;
  defaultValue: T;
  body?: any;
  method?: FetchMethod;
  initialValue?: T;
  transformResponse?: (response: Response) => Promise<T>;
  readOnMount?: boolean;
}
export function useApiResource<T>({
  useDvApiUrl = false,
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

  const previousInitialValue = React.useRef(initialValue);
  const previousDefaultValue = React.useRef(defaultValue);
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
        url: useDvApiUrl
          ? `${apiContext.dvApiUri}${fUrl}`
          : `${apiContext.apiUri}${fUrl}`,
      });
      if (!response.ok) {
        throw new Error(response.statusText);
      }
      let data: T = await transformResponse(response);
      if (defaultValue) {
        data = deepmerge(defaultValue, data);
      }
      setResource(data);
      setHasData(true);
      setLoading(false);
      return data;
    } catch (e) {
      setHasData(false);
      setLoading(false);
      setError(e);
    }
    return null;
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
    if (
      !equal(previousInitialValue.current, initialValue) ||
      !equal(previousDefaultValue.current, defaultValue)
    ) {
      previousInitialValue.current = initialValue;
      previousDefaultValue.current = defaultValue;
      setHasData(!!initialValue);
      setResource(initialValue || defaultValue);
      if (previousUrl.current === url && readOnMount) {
        previousUrl.current = undefined;
      }
    }

    if (previousUrl.current !== url && readOnMount) {
      read();
      previousUrl.current = url;
    }
  }, [
    defaultValue,
    initialValue,
    previousDefaultValue,
    previousInitialValue,
    previousUrl,
    read,
    readOnMount,
    setHasData,
    setResource,
    url,
  ]);

  return { resource, loading, error, hasData, read };
}
