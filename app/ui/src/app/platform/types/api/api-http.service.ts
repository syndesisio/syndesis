import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { StringMap, FileMap } from '@syndesis/ui/platform';
import {
  ApiEndpoint,
  ApiRequestProgress,
  ApiRequestOptions,
  ApiUploadOptions
} from '@syndesis/ui/platform/types/api/api.models';

@Injectable()
export abstract class ApiHttpService {
  /**
   * An observable stream emitting `ApiRequestProgress` object instances, to be used to gauge progress
   * when submitting multipart messages or uploading files.
   */
  abstract uploadProgressEvent$: Observable<ApiRequestProgress>;

  /**
   * Allows for building REST API Urls by composition, matching the key givn with the internal API endpoints configured for that module.
   * @param endpointKey Unique key of the endpoint, available either at the global config or the module API endpoints config.
   * @param endpointParams Additional parameters, either an array of strings or an object literal containing named key/values.
   * @returns {string} A fullly populated URL to submit REST requests to.
   * @example For an endpoint named `employees` configured on a domain level and injected into the `ApiConfigService`
   *          provider at the domain's NgModule:
   *
   * ```typescript
   * const endpointUrl = this.getEndpointUrl('employees', { role: 'director', name: 'John' });
   * // Outputs: 'https://syndesis.192.168.64.17.xip.io/api/v2/employees/director/john'
   * ```
   */
  abstract getEndpointUrl(
    endpointKey: string,
    ...endpointParams: any[]
  ): string;

  /**
   * Will receive an endpoint key and additional parameters and after composing the API endpoint URL internally, will allow
   * for submitting Http requests to it with its own payload if required.
   * @param endpointKey Unique key of the endpoint, available either at the global config or the module API endpoints config.
   * @param endpointParams Additional parameters, either an array of strings or an object literal containing named key/values.
   * @returns {ApiEndpoint} A chainable interface that exposes methods to send GET/POST/PUT/DELETE
   *                        requests to the endpoint/params combo selected.
   * @example For the same endpoint as in the previous example, we send a PUT request to update name:
   *
   * ```typescript
   * this.setEndpointUrl('employees', { role: 'director', name: 'John' }).put({ name: 'Jane' });
   * ```
   */
  abstract setEndpointUrl(
    endpointKey: string,
    ...endpointParams: any[]
  ): ApiEndpoint;

  /**
   * Sends a GET request to the selected API endpoint URL.
   * @param endpoint URL of the API endpoint to use, provided by `ApiConfigService`.
   * @param options Optional. Adds additional metadata to the request, such as custom headers or response types, credentials, etc.
   * @returns {Observable<T>} Observable typed as T containing the server response.
   */
  abstract get<T>(
    endpoint: string | any[],
    options?: ApiRequestOptions | any
  ): Observable<T>;

  /**
   * Sends a POST request containing the given body to the selected API endpoint URL.
   * @param endpoint URL of the API endpoint to use, provided by `ApiConfigService`.
   * @param body Request body. Can be any type.
   * @param options Optional. Adds additional metadata to the request, such as custom headers or response types, credentials, etc.
   * @returns {Observable<T>} Observable typed as T containing the server response.
   */
  abstract post<T>(
    endpoint: string | any[],
    body: any,
    options?: ApiRequestOptions | any
  ): Observable<T>;

  /**
   * Sends a PUT request containing the given body to the selected API endpoint URL.
   * @param endpoint URL of the API endpoint to use, provided by `ApiConfigService`.
   * @param body Request body. Can be any type.
   * @param options Optional. Adds additional metadata to the request, such as custom headers or response types, credentials, etc.
   * @returns {Observable<T>} Observable typed as T containing the server response.
   */
  abstract put<T>(
    endpoint: string | any[],
    body: any,
    options?: ApiRequestOptions | any
  ): Observable<T>;

  /**
   * Sends a PATCH request containing the given body to the selected API endpoint URL.
   * @param endpoint URL of the API endpoint to use, provided by `ApiConfigService`.
   * @param body Request body. Can be any type.
   * @param options Optional. Adds additional metadata to the request, such as custom headers or response types, credentials, etc.
   * @returns {Observable<T>} Observable typed as T containing the server response.
   */
  abstract patch<T>(
    endpoint: string | any[],
    body: any,
    options?: ApiRequestOptions | any
  ): Observable<T>;

  /**
   * Sends a DELETE request to the selected API endpoint URL.
   * @param endpoint URL of the API endpoint to use, provided by `ApiConfigService`.
   * @param options Optional. Adds additional metadata to the request, such as custom headers or response types, credentials, etc.
   * @returns {Observable<T>} Observable typed as T containing the server response.
   */
  abstract delete<T>(
    endpoint: string | any[],
    options?: ApiRequestOptions | any
  ): Observable<T>;

  /**
   * Sends a multipart message wrapped by a POST/PUT request to the API, allowing one or several file uploads along with JSON objects.
   * @param endpoint URL of the API endpoint to use, provided by `ApiConfigService`.
   * @param fileMap Hash library containing key/value pairs of named files.
   * @param body Request body. Can be any type.
   * @param options Optional. Allows for adding additional metadata to the request, such as custom headers or response types, methods, etc.
   * @returns {Observable<T>} Observable typed as T containing the server response.
   */
  abstract upload<T>(
    endpoint: string | any[],
    fileMap?: FileMap,
    body?: StringMap<any>,
    options?: ApiUploadOptions
  ): Observable<T>;
}
