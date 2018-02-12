import { Injectable, Inject } from '@angular/core';

import { ApiConfigService, API_ENDPOINTS, Endpoints } from '@syndesis/ui/platform';
import { ConfigService } from '@syndesis/ui/config.service';

@Injectable()
export class ApiConfigProviderService extends ApiConfigService {
  baseUrl: string;
  endpoints: Endpoints;

  constructor(@Inject(API_ENDPOINTS) apiEndpoints: Endpoints, configService: ConfigService) {
    super();

    configService.asyncSettings$.subscribe(({ apiBase, apiEndpoint }) => {
      this.baseUrl = `${apiBase}${apiEndpoint}`;
    });

    this.registerEndpoints(apiEndpoints);
  }

  registerEndpoints(apiEndpoints: Endpoints): void {
    const initialEndpoints = this.endpoints && Array.isArray(this.endpoints) ? this.endpoints[0] : this.endpoints || {};
    const newEndpoints = Array.isArray(apiEndpoints) ? apiEndpoints[0] : apiEndpoints;

    this.endpoints = {
      ...initialEndpoints,
      ...newEndpoints
    };
  }
}
