import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpRequest, HttpEventType, HttpProgressEvent, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';

import { ApiHttpService, ApiEndpoint, ApiRequestProgress, ActionReducerError, StringMap, FileMap } from '@syndesis/ui/platform';
import { ConfigService } from '@syndesis/ui/config.service';

const DEFAULT_ERROR_MSG = 'An unexpected HTTP error occured. Please check stack strace';

@Injectable()
export class ApiHttpProviderService extends ApiHttpService {
  private uploadProgressSubject = new Subject<ApiRequestProgress>();
  private apiBaseHost: string;
  private apiChildEndpoints: StringMap<string>;

  constructor(private httpClient: HttpClient, private configService: ConfigService) {
    super();

    const { apiBase, apiEndpoint, apiChildEndpoints } = this.configService.getSettings();
    this.apiBaseHost = `${apiBase}${apiEndpoint}`;
    this.apiChildEndpoints = apiChildEndpoints || {};
  }

  getEndpointUrl(endpointKey: string, ...endpointParams: any[]): string {
    let url = this.apiChildEndpoints[endpointKey] || endpointKey;

    if (endpointParams && endpointParams.length == 1 && endpointParams[0] === Object(endpointParams[0])) {
      url = this.replaceNameMatches(url, endpointParams[0]);
    } else if (endpointParams && endpointParams.length >= 1) {
      url = this.replaceIndexMatches(url, endpointParams);
    }

    if (!url.startsWith('http')) {
      url = `${this.apiBaseHost}${url}`;
    }

    return url;
  }

  setEndpointUrl(endpointKey: string, ...endpointParams: any[]): ApiEndpoint {
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);

    return {
      url,
      get: <T>() => this.get<T>(url),
      post: <T>(body: any) => this.post<T>([endpointKey, ...endpointParams], body),
      put: <T>(body: any) => this.put<T>([endpointKey, ...endpointParams], body),
      delete: <T>() => this.delete<T>(url),
      upload: <T>(fileMap?: FileMap, body?: StringMap<any>, verb?: string) => {
        return this.upload<T>([endpointKey, ...endpointParams], fileMap, body, verb);
      }
    };
  }

  get<T>(endpoint: string | any[]): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(endpoint);
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    return this.httpClient
      .get<T>(url)
      .catch(error => Observable.throw(this.catchError(error)));
  }

  post<T>(endpoint: string | any[], body?: any): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(endpoint);
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    return this.httpClient
      .post<T>(url, body)
      .catch(error => Observable.throw(this.catchError(error)));
  }

  put<T>(endpoint: string | any[], body: any): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(endpoint);
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    return this.httpClient
      .put<T>(url, body)
      .catch(error => Observable.throw(this.catchError(error)));
  }

  delete<T>(endpoint: string | any[]): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(endpoint);
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    return this.httpClient
      .delete<T>(url)
      .catch(error => Observable.throw(this.catchError(error)));
  }

  get uploadProgressEvent$(): Observable<ApiRequestProgress> {
    return this.uploadProgressSubject.asObservable();
  }

  upload<T>(endpoint: string | any[], fileMap: FileMap, body?: StringMap<any>, verb = 'POST'): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(endpoint);
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    const headers = new HttpHeaders();

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

    const request = new HttpRequest(verb, url, multipartFormData, { headers, reportProgress: true });

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
