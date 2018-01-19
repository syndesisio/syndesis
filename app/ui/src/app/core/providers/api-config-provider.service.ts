import { Injectable, Inject } from '@angular/core';

import { ApiConfigService, API_ENDPOINTS, Endpoints } from '@syndesis/ui/platform';
import { ConfigService } from '@syndesis/ui/config.service';

@Injectable()
export class ApiConfigProviderService extends ApiConfigService {
  baseUrl: string;
  endpoints: Endpoints;

  constructor(@Inject(API_ENDPOINTS) apiEndpoints: Endpoints, configService: ConfigService) {
    super();

    const { apiBase, apiEndpoint } = configService.getSettings();
    this.baseUrl = `${apiBase}${apiEndpoint}`;

    this.endpoints = Object.keys(apiEndpoints).reduce((aggregatedEndpoints, endpointKey) => {
      const newEndpoint = {};
      newEndpoint[endpointKey] = apiEndpoints[endpointKey];
      return {
        ...aggregatedEndpoints,
        ...newEndpoint
      }[0];
    }, {});
  }
}
