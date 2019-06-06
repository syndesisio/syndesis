export type FetchMethod = 'GET' | 'PATCH' | 'PUT' | 'POST' | 'DELETE';
export interface IFetchHeaders {
  [s: string]: string;
}
export interface IFetch {
  url: string;
  method: FetchMethod;
  headers?: IFetchHeaders;
  body?: any;

  /**
   * Indicates if the content type should be included in the headers. Defaults to `true`. When using a
   * content type of 'multipart/form-data' this should be set to `false` but you still need to set the
   * content type.
   */
  includeContentType?: boolean;

  /**
   * Defaults to 'application/json; charset=utf-8' if not set.
   */
  contentType?: string;

  /**
   * Indicates if 'accept' should be included in the headers. Defaults to `true`.
   */
  includeAccept?: boolean;

  /**
   * Defaults to 'application/json,text/plain,*&#47*' if not set.
   */
  accept?: string;

  /**
   * Indicates if a 'Referer Policy' of 'no-referrer' should be included.
   */
  includeReferrerPolicy?: boolean;

  /**
   * Whether or not to stringify the data to JSON, overrides the content type
   */
  stringifyBody?: boolean;
}

const referrerPolicyId = 'referrerPolicy';
const acceptId = 'Accept';
const contentTypeId = 'Content-Type';

export function callFetch({
  url,
  method,
  headers = {},
  body,
  includeContentType = true,
  contentType = 'application/json; charset=utf-8',
  includeAccept = true,
  accept = 'application/json,text/plain,*/*',
  includeReferrerPolicy = true,
  stringifyBody = true,
}: IFetch) {
  headers = { ...headers };
  if (includeAccept && !(acceptId in headers)) {
    headers[acceptId] = accept;
  }
  if (includeContentType && !(contentTypeId in headers)) {
    headers[contentTypeId] = contentType;
  }
  if (includeReferrerPolicy && !(referrerPolicyId in headers)) {
    headers[referrerPolicyId] = 'no-referrer';
  }
  const data =
    headers[contentTypeId] &&
    headers[contentTypeId].includes('application/json') &&
    stringifyBody
      ? JSON.stringify(body)
      : body;

  return fetch(url, {
    body: data,
    cache: 'no-cache',
    credentials: 'include',
    headers: {
      ...headers,
    },
    method,
    mode: 'cors',
    redirect: 'follow',
  });
}
