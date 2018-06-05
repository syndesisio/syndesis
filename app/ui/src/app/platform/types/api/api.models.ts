import { InjectionToken } from '@angular/core';
import { HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { StringMap, FileMap } from '@syndesis/ui/platform';

export declare type Endpoints = StringMap<string>;

export const API_ENDPOINTS = new InjectionToken('API_ENDPOINTS');

export interface ApiErrorMessage {
  developerMsg?: string;
  userMsg?: string;
  userMsgDetail?: string;
  errorCode?: number;
}

export interface ApiErrors {
  errors?: {
    status: number;
    statusText: string;
    messages: Array<ApiErrorMessage>;
  };
}

export interface ApiResponse extends ApiErrors {
  readonly id?: string;
  kind?: string;
}

export interface ApiRequestOptions {
  headers?: HttpHeaders;
  params?: HttpParams;
  responseType?: 'arraybuffer' | 'blob' | 'json' | 'text';
  withCredentials?: boolean;
}

export interface ApiUploadOptions extends ApiRequestOptions {
  method?: 'POST' | 'PUT';
}

export interface ApiEndpoint {
  url: string;
  get<T>(options?: ApiRequestOptions): Observable<T>;
  post<T>(body: any, options?: ApiRequestOptions): Observable<T>;
  put<T>(body: any, options?: ApiRequestOptions): Observable<T>;
  patch<T>(body: any, options?: ApiRequestOptions): Observable<T>;
  delete<T>(body?: any, options?: ApiRequestOptions): Observable<T>;
  upload<T>(
    fileMap?: FileMap,
    body?: StringMap<any>,
    options?: ApiUploadOptions
  ): Observable<T>;
}

export interface ApiRequestProgress {
  percentage: number;
  isComplete: boolean;
  bytesLoaded?: number;
  bytesTotal?: number;
}
