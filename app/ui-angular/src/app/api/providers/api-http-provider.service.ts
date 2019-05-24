import { of as observableOf, Observable, Subject, throwError } from 'rxjs';

import { map, filter, tap, catchError } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpHeaders,
  HttpRequest,
  HttpEventType,
  HttpProgressEvent,
  HttpResponse,
  HttpUrlEncodingCodec
} from '@angular/common/http';

import {
  ApiHttpService,
  ApiConfigService,
  ApiEndpoint,
  ApiRequestProgress,
  ActionReducerError,
  ApiRequestOptions,
  ApiUploadOptions,
  StringMap,
  FileMap
} from '@syndesis/ui/platform';

const DEFAULT_ERROR_MSG =
  'An unexpected HTTP error occured. Please check stack strace';
const PROPAGATE_EXCEPTIONS = true;

@Injectable()
export class ApiHttpProviderService extends ApiHttpService {
  private uploadProgressSubject = new Subject<ApiRequestProgress>();
  private httpUrlEncodingCodec = new HttpUrlEncodingCodec();

  constructor(
    private httpClient: HttpClient,
    private apiConfigService: ApiConfigService
  ) {
    super();
  }

  getEndpointUrl(endpointKey: string, ...endpointParams: any[]): string {
    const apiBaseHost = this.apiConfigService.baseUrl;
    const apiEndpoints = this.apiConfigService.endpoints || {};
    let endpoint = apiEndpoints[endpointKey] || endpointKey;

    if (
      endpointParams &&
      endpointParams.length == 1 &&
      endpointParams[0] === Object(endpointParams[0])
    ) {
      endpoint = this.replaceNameMatches(endpoint, endpointParams[0]);
    } else if (endpointParams && endpointParams.length >= 1) {
      endpoint = this.replaceIndexMatches(endpoint, endpointParams);
    }

    if (!endpoint.startsWith('http')) {
      endpoint = `${apiBaseHost}${endpoint}`;
    }

    return endpoint;
  }

  setEndpointUrl(endpointKey: string, ...endpointParams: any[]): ApiEndpoint {
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);

    return {
      url,
      get: <T>(options?: ApiRequestOptions | any) => this.get<T>(url, options),
      post: <T>(body: any, options?: ApiRequestOptions | any) =>
        this.post<T>([endpointKey, ...endpointParams], body, options),
      put: <T>(body: any, options?: ApiRequestOptions | any) =>
        this.put<T>([endpointKey, ...endpointParams], body, options),
      patch: <T>(body: any, options?: ApiRequestOptions | any) =>
        this.patch<T>([endpointKey, ...endpointParams], body, options),
      delete: <T>(options?: ApiRequestOptions | any) =>
        this.delete<T>(url, options),
      upload: <T>(
        fileMap?: FileMap,
        body?: StringMap<any>,
        options?: ApiUploadOptions
      ) => {
        return this.upload<T>(
          [endpointKey, ...endpointParams],
          fileMap,
          body,
          options
        );
      }
    };
  }

  get<T>(
    endpoint: string | any[],
    options?: ApiRequestOptions | any
  ): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(
      endpoint
    );
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    const headers = this.getHeaders(options);
    return this.httpClient
      .get(url, { headers, ...options })
      .pipe(catchError(error => this.handleError(error)));
  }

  post<T>(
    endpoint: string | any[],
    body?: any,
    options?: ApiRequestOptions | any
  ): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(
      endpoint
    );
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    const headers = this.getHeaders(options);
    return this.httpClient
      .post<T>(url, body, { headers, ...options })
      .pipe(catchError(error => this.handleError(error)));
  }

  put<T>(
    endpoint: string | any[],
    body: any,
    options?: ApiRequestOptions | any
  ): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(
      endpoint
    );
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    const headers = this.getHeaders(options);
    return this.httpClient
      .put<T>(url, body, { headers, ...options })
      .pipe(catchError(error => this.handleError(error)));
  }

  patch<T>(
    endpoint: string | any[],
    body: any,
    options?: ApiRequestOptions | any
  ): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(
      endpoint
    );
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    const headers = this.getHeaders(options);
    return this.httpClient
      .patch<T>(url, body, { headers, ...options })
      .pipe(catchError(error => this.handleError(error)));
  }

  delete<T>(
    endpoint: string | any[],
    options?: ApiRequestOptions | any
  ): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(
      endpoint
    );
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    const headers = this.getHeaders(options);
    return this.httpClient
      .delete<T>(url, { headers, ...options })
      .pipe(catchError(error => this.handleError(error)));
  }

  get uploadProgressEvent$(): Observable<ApiRequestProgress> {
    return this.uploadProgressSubject.asObservable();
  }

  upload<T>(
    endpoint: string | any[],
    fileMap: FileMap,
    body?: StringMap<any>,
    options?: ApiUploadOptions
  ): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(
      endpoint
    );
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    const method = options && options.method ? options.method : 'POST';
    const requestOptions = {
      reportProgress: true,
      ...options
    };

    const multipartFormData = new FormData();

    for (const key in fileMap) {
      if (fileMap.hasOwnProperty(key)) {
        multipartFormData.append(key, fileMap[key]);
      }
    }

    if (body) {
      for (const key in body) {
        if (body.hasOwnProperty(key)) {
          if (body[key] instanceof Object) {
            multipartFormData.append(
              key,
              new Blob([JSON.stringify(body[key])], {
                type: 'application/json'
              }),
              key
            );
          } else {
            multipartFormData.append(key, body[key]);
          }
        }
      }
    }

    const request = new HttpRequest(
      method,
      url,
      multipartFormData,
      requestOptions
    );

    return this.httpClient.request(request).pipe(
      tap(requestEvent => {
        if (requestEvent.type === HttpEventType.UploadProgress) {
          this.emitProgressEvent(requestEvent);
        }
        return requestEvent;
      }),
      filter(requestEvent => requestEvent.type === HttpEventType.Response),
      map(requestEvent => requestEvent as HttpResponse<T>),
      map(requestEvent => requestEvent.body),
      catchError(error => this.handleError(error.error))
    );
  }

  private getHeaders(options: ApiRequestOptions | any) {
    const headers = new HttpHeaders({
      ...(options ? options.headers : undefined)
    });
    return headers;
  }

  private emitProgressEvent(httpProgressEvent?: HttpProgressEvent): void {
    let progress: ApiRequestProgress;

    if (httpProgressEvent) {
      progress = {
        percentage: Math.round(
          100 * httpProgressEvent.loaded / httpProgressEvent.total
        ),
        bytesLoaded: httpProgressEvent.loaded,
        bytesTotal: httpProgressEvent.total,
        isComplete: httpProgressEvent.loaded == httpProgressEvent.total
      };
    } else {
      progress = { isComplete: false, percentage: 0 };
    }

    this.uploadProgressSubject.next(progress);
  }

  private deconstructEndpointParams(
    endpoint: string | any[]
  ): { endpointKey: string; endpointParams: any[] } {
    let endpointKey,
      endpointParams = [];
    if (Array.isArray(endpoint) && endpoint.length >= 1) {
      endpointKey = endpoint[0];
      endpointParams = endpoint.slice(1);
    } else {
      endpointKey = endpoint;
    }

    return { endpointKey, endpointParams };
  }

  private replaceNameMatches(
    endpointTemplate: string,
    paramsMap: StringMap<any>
  ): string {
    const matched = [];
    const answer = endpointTemplate.replace(
      /\{(\D*?)\}/g,
      (fullMatch, ...matchGroups) => {
        const match = matchGroups[0].trim();
        matched.push(match);
        return this.httpUrlEncodingCodec.encodeValue(paramsMap[match]);
      }
    );
    // Turn any remaining parameters into query params
    const remainder = Object.keys(paramsMap).filter(
      x => matched.indexOf(x) === -1
    );
    const queryParams = remainder
      .map(key => {
        const value = paramsMap[key];
        if (value === undefined || value === null) {
          return undefined;
        }
        // expand an array value in the param map to multiple query params
        if (typeof value === 'string') {
          return (
            this.httpUrlEncodingCodec.encodeKey(key) +
            '=' +
            this.httpUrlEncodingCodec.encodeValue(value)
          );
        } else if (Array.isArray(value)) {
          return value
            .map(
              val =>
                this.httpUrlEncodingCodec.encodeKey(key) +
                '=' +
                this.httpUrlEncodingCodec.encodeValue(val)
            )
            .join('&');
        } else {
          // not supported, drop it
          return undefined;
        }
      })
      .filter(x => x)
      .join('&');
    if (queryParams.length) {
      // cater for the possibility that the template has query params in it
      if (answer.indexOf('?') !== -1) {
        return answer + '&' + queryParams;
      } else {
        return answer + '?' + queryParams;
      }
    } else {
      return answer;
    }
  }

  private replaceIndexMatches(
    endpointTemplate: string,
    ...params: any[]
  ): string {
    if (Array.isArray(params) && params.length > 0) {
      return endpointTemplate.replace(
        /\{(\d*?)\}/g,
        (fullMatch, ...matchGroups) => {
          const index = parseInt(matchGroups[0], 10);
          return params[index];
        }
      );
    } else if (params && params.toString() !== '') {
      return endpointTemplate.replace(/\{(\d*?)\}/g, params.toString());
    }
    return endpointTemplate;
  }

  private handleError(error: any): Observable<any> {
    const httpError = this.catchError(error);
    return PROPAGATE_EXCEPTIONS
      ? throwError(httpError)
      : observableOf(httpError);
  }

  private catchError(response): ActionReducerError {
    const error = response.error;
    const message = error ? error.userMsg : response.userMsg;
    const debugMessage = error ? error.developerMsg : response.developerMsg;
    return {
      message: message || DEFAULT_ERROR_MSG,
      debugMessage: debugMessage || DEFAULT_ERROR_MSG,
      status: response.errorCode || response.status,
      data: response.error ? response.error : response
    };
  }
}
