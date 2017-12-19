import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { ApiHttpService, ApiEndpoint, StringMap } from '@syndesis/ui/platform';
import { ConfigService } from '@syndesis/ui/config.service';

@Injectable()
export class ApiHttpProviderService extends ApiHttpService {
  private apiBaseHost: string;
  private apiChildEndpoints: StringMap<string>;

  constructor(private httpClient: HttpClient, private configService: ConfigService) {
    super();

    const { apiBase, apiEndpoint, apiChildEndpoints } = this.configService.getSettings();
    this.apiBaseHost = `${apiBase}${apiEndpoint}`;
    this.apiChildEndpoints = apiChildEndpoints;
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
      delete: <T>() => this.delete<T>(url)
    };
  }

  get<T>(endpoint: string | any[]): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(endpoint);
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    return this.httpClient.get<T>(url);
  }

  post<T>(endpoint: string | any[], body?: any): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(endpoint);
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    return this.httpClient.post<T>(url, body);
  }

  put<T>(endpoint: string | any[], body: any): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(endpoint);
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    return this.httpClient.put<T>(url, body);
  }

  delete<T>(endpoint: string | any[]): Observable<T> {
    const { endpointKey, endpointParams } = this.deconstructEndpointParams(endpoint);
    const url = this.getEndpointUrl(endpointKey, ...endpointParams);
    return this.httpClient.delete<T>(url);
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
}
