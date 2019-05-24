import { Injectable, Inject } from '@angular/core';
import { ApiConfigService, API_ENDPOINTS, Endpoints } from '@syndesis/ui/platform';

@Injectable()
export class ApiEndpointsLazyLoaderService {
  constructor(@Inject(API_ENDPOINTS) apiEndpoints: Endpoints, apiConfigService: ApiConfigService) {
    apiConfigService.registerEndpoints(apiEndpoints);
  }
}
