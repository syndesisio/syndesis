export interface IDVFetchHeaders {
  [s: string]: string;
}
export interface IDVFetch {
  url: string;
  method: 'GET' | 'PUT' | 'POST';
  headers?: IDVFetchHeaders;
  body?: any;
  contentType?: string;
  accept?: string;
}
export function callDVFetch({
  url,
  method,
  headers = {},
  body,
  contentType = 'application/json; charset=utf-8',
  accept = 'application/json,text/plain,*/*',
}: IDVFetch) {
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
