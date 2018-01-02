import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { StringMap, FileMap } from '@syndesis/ui/platform';
import { ApiResponse, ApiEndpoint, ApiRequestProgress } from './api.models';

@Injectable()
export abstract class ApiHttpService {
  abstract uploadProgressEvent$: Observable<ApiRequestProgress>;

  abstract getEndpointUrl(endpointKey: string, ...endpointParams: any[]): string;

  abstract setEndpointUrl(endpointKey: string, ...endpointParams: any[]): ApiEndpoint;

  abstract get<T>(endpoint: string | any[]): Observable<T>;

  abstract post<T>(endpoint: string | any[], body: any): Observable<T>;

  abstract put<T>(endpoint: string | any[], body: any): Observable<T>;

  abstract delete<T>(endpoint: string | any[]): Observable<T>;

  abstract upload<T>(endpoint: string | any[], fileMap?: FileMap, body?: StringMap<any>): Observable<T>;
}
