export interface IFetchHeaders {
  [s: string]: string;
}
export interface IFetch {
  url: string;
  method: 'GET' | 'PUT' | 'POST' | 'DELETE';
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
}

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
}: IFetch) {
  if (includeAccept) {
    const acceptId = 'Accept';
    headers[acceptId] = accept;
  }

  if (includeContentType) {
    const contentTypeId = 'Content-Type';
    headers[contentTypeId] = contentType;
  }

  if (includeReferrerPolicy) {
    const referrerPolicyId = 'referrerPolicy';
    headers[referrerPolicyId] = 'no-referrer';
  }

  return fetch(url, {
    body: contentType.includes('application/json')
      ? JSON.stringify(body)
      : body,
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
