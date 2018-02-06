import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpRequest, HttpEventType, HttpProgressEvent, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';

import {
  ApiHttpService, ApiConfigService,
  ApiEndpoint, ApiRequestProgress, ActionReducerError,
  ApiRequestOptions, ApiUploadOptions,
  StringMap, FileMap
} from '@syndesis/ui/platform';
import { ConfigService } from '@syndesis/ui/config.service';

const DEFAULT_ERROR_MSG = 'An unexpected HTTP error occured. Please check stack strace';

@Injectable()
export class ApiHttpProviderService extends ApiHttpService {
  private uploadProgressSubject = new Subject<ApiRequestProgress>();

  constructor(private httpClient: HttpClient, private apiConfigService: ApiConfigService) {
    super();
  }

  getEndpointUrl(endpointKey: string, ...endpointParams: any[]): string {
    const apiBaseHost = this.apiConfigService.baseUrl;
    const apiEndpoints = this.apiConfigService.endpoints || {};
    let endpoint = apiEndpoints[endpointKey] || endpointKey;

    if (endpointParams && endpointParams.length == 1 && endpointParams[0] === Object(endpointParams[0])) {
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
      post: <T>(body: any, options?: ApiRequestOptions | any) => this.post<T>([endpointKey, ...endpointParams], body, options),
      put: <T>(body: any, options?: ApiRequestOptions | any) => this.put<T>([endpointKey, ...endpointParams], body, options),
      delete: <T>(options?: ApiRequestOptions | any) => this.delete<T>(url, options),
      upload: <T>(fileMap?: FileMap, body?: StringMap<any>, options?: ApiUploadOptions) => {
        return this.upload<T>([endpointKey, ...endpointParams], fileMap, body, options);
      }
    };
  }

  get<T>(endpoint: string | any[], options?: ApiRequestOptions | any): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(endpoint);
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    return this.httpClient
      .get(url, options)
      .catch(error => Observable.throw(this.catchError(error)));
  }

  post<T>(endpoint: string | any[], body?: any, options?: ApiRequestOptions | any): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(endpoint);
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    return this.httpClient
      .post<T>(url, body, options)
      .catch(error => Observable.throw(this.catchError(error)));
  }

  put<T>(endpoint: string | any[], body: any, options?: ApiRequestOptions | any): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(endpoint);
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    return this.httpClient
      .put<T>(url, body, options)
      .catch(error => Observable.throw(this.catchError(error)));
  }

  delete<T>(endpoint: string | any[], options?: ApiRequestOptions | any): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(endpoint);
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    return this.httpClient
      .delete<T>(url, options)
      .catch(error => Observable.throw(this.catchError(error)));
  }

  get uploadProgressEvent$(): Observable<ApiRequestProgress> {
    return this.uploadProgressSubject.asObservable();
  }

  upload<T>(endpoint: string | any[], fileMap: FileMap, body?: StringMap<any>, options?: ApiUploadOptions): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(endpoint);
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    const method = options && options.method ? options.method : 'POST';
    const headers = new HttpHeaders();
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
            multipartFormData.append(key, new Blob([JSON.stringify(body[key])], { type: 'application/json' }), key);
          } else {
            multipartFormData.append(key, body[key]);
          }
        }
      }
    }

    const request = new HttpRequest(method, url, multipartFormData, requestOptions);

    return this.httpClient.request(request)
      .do(requestEvent => {
        if (requestEvent.type === HttpEventType.UploadProgress) {
          this.emitProgressEvent(requestEvent);
        }
        return requestEvent;
      })
      .filter(requestEvent => requestEvent.type === HttpEventType.Response)
      .map(requestEvent => requestEvent as HttpResponse<T>)
      .map(requestEvent => requestEvent.body)
      .catch(error => Observable.throw(this.catchError(JSON.parse(error.error))));
  }

  private emitProgressEvent(httpProgressEvent?: HttpProgressEvent): void {
    let progress: ApiRequestProgress;

    if (httpProgressEvent) {
      progress = {
        percentage: Math.round(100 * httpProgressEvent.loaded / httpProgressEvent.total),
        bytesLoaded: httpProgressEvent.loaded,
        bytesTotal: httpProgressEvent.total,
        isComplete: httpProgressEvent.loaded == httpProgressEvent.total
      };
    } else {
      progress = { isComplete: false, percentage: 0 };
    }

    this.uploadProgressSubject.next(progress);
  }

  private deconstructEndpointParams(endpoint: string | any[]): { endpointKey: string; endpointParams: any[]; } {
    let endpointKey, endpointParams = [];
    if (Array.isArray(endpoint) && endpoint.length >= 1) {
      endpointKey = endpoint[0];
      endpointParams = endpoint.slice(1);
    } else {
      endpointKey = endpoint;
    }

    return { endpointKey, endpointParams };
  }

  private replaceNameMatches(endpointTemplate: string, paramsMap: StringMap<any>): string {
    return endpointTemplate.replace(/\{(\D*?)\}/g, (fullMatch, ...matchGroups) => {
      return paramsMap[matchGroups[0].trim()];
    });
  }

  private replaceIndexMatches(endpointTemplate: string, ...params: any[]): string {
    if (Array.isArray(params) && params.length > 0) {
      return endpointTemplate.replace(/\{(\d*?)\}/g, (fullMatch, ...matchGroups) => {
        const index = parseInt(matchGroups[0], 10);
        return params[index];
      });
    } else if (params && params.toString() !== '') {
      return endpointTemplate.replace(/\{(\d*?)\}/g, params.toString());
    }

    return endpointTemplate;
  }

  private catchError(error): ActionReducerError {
    return {
      message: error.userMsg || DEFAULT_ERROR_MSG,
      debugMessage: error.developerMsg || DEFAULT_ERROR_MSG,
      status: error.errorCode,
    };
  }
}
