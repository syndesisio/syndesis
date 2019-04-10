import { Injectable } from '@angular/core';
import { Observable, of, Subject } from 'rxjs';

import {
  ApiHttpService,
  ApiEndpoint,
  ApiRequestProgress,
  ApiRequestOptions,
  ApiUploadOptions,
  StringMap,
  FileMap
} from '@syndesis/ui/platform';

@Injectable()
export class TestApiHttpService extends ApiHttpService {
  private uploadProgressSubject = new Subject<ApiRequestProgress>();

  constructor() {
    super();
    this.uploadProgressSubject.next({
      percentage: 0,
      isComplete: false,
      bytesLoaded: 0,
      bytesTotal: 0
    });
  }

  getEndpointUrl(endpointKey: string, ...endpointParams: any[]): string {
    return '';
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
        this.put<T>([endpointKey, ...endpointParams], body, options),
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
    return of(null as T);
  }

  post<T>(
    endpoint: string | any[],
    body?: any,
    options?: ApiRequestOptions | any
  ): Observable<T> {
    return of(null as T);
  }

  put<T>(
    endpoint: string | any[],
    body: any,
    options?: ApiRequestOptions | any
  ): Observable<T> {
    return of(null as T);
  }

  patch<T>(
    endpoint: string | any[],
    body: any,
    options?: ApiRequestOptions | any
  ): Observable<T> {
    return of(null as T);
  }

  delete<T>(
    endpoint: string | any[],
    options?: ApiRequestOptions | any
  ): Observable<T> {
    return of(null as T);
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
    this.emitProgressEvent();

    return of(null as T);
  }

  private emitProgressEvent(): void {
    this.uploadProgressSubject.next({
      percentage: 100,
      isComplete: true,
      bytesLoaded: 1000,
      bytesTotal: 1000
    });
  }
}
