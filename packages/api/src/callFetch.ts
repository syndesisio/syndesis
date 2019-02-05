export interface IFetchHeaders {
  [s: string]: string;
}
export interface IFetch {
  url: string;
  method: 'GET' | 'PUT' | 'POST';
  headers?: IFetchHeaders;
  body?: any;
  contentType?: string;
  accept?: string;
}
export function callFetch({
  url,
  method,
  headers = {},
  body,
  contentType = 'application/json; charset=utf-8',
  accept = 'application/json,text/plain,*/*',
}: IFetch) {
  return fetch(url, {
    body: body ? JSON.stringify(body) : undefined,
    cache: 'no-cache',
    credentials: 'include',
    headers: {
      Accept: accept,
      'Content-Type': contentType,
      ...headers,
    },
    method,
    mode: 'cors',
    redirect: 'follow',
    referrerPolicy: 'no-referrer',
  });
}
