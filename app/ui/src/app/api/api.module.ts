import { ModuleWithProviders, NgModule, Optional } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';

import { ApiHttpService, ApiConfigService, Endpoints, API_ENDPOINTS } from '@syndesis/ui/platform';
import * as SYNDESIS_PROVIDERS from './providers';

export function endpointsLazyLoaderFactory(apiEndpoints: Endpoints, apiConfigService: ApiConfigService) {
  return new SYNDESIS_PROVIDERS.ApiEndpointsLazyLoaderService(apiEndpoints, apiConfigService);
}

@NgModule({
  imports: [CommonModule, HttpClientModule],
})
export class ApiModule {
  constructor(@Optional() apiEndpointsLazyLoaderService: SYNDESIS_PROVIDERS.ApiEndpointsLazyLoaderService) { }

  static forRoot(apiEndpoints?: Endpoints): Array<ModuleWithProviders> {
    return [{
      ngModule: ApiModule,
      providers: [{
        provide: API_ENDPOINTS,
        multi: true,
        useValue: apiEndpoints || {}
      },
      SYNDESIS_PROVIDERS.ApiConfigProviderService,
      {
        provide: ApiConfigService,
        useClass: SYNDESIS_PROVIDERS.ApiConfigProviderService
      },
      SYNDESIS_PROVIDERS.ApiHttpProviderService,
      {
        provide: ApiHttpService,
        useClass: SYNDESIS_PROVIDERS.ApiHttpProviderService
      },
      ]
    },
    ];
  }

  static forChild(apiEndpoints: Endpoints): Array<ModuleWithProviders> {
    return [{
      ngModule: ApiModule,
      providers: [{
        provide: API_ENDPOINTS,
        multi: true,
        useValue: apiEndpoints
      },
      SYNDESIS_PROVIDERS.ApiEndpointsLazyLoaderService,
      {
        provide: SYNDESIS_PROVIDERS.ApiEndpointsLazyLoaderService,
        useFactory: endpointsLazyLoaderFactory,
        deps: [API_ENDPOINTS, ApiConfigService]
      }]
    }];
  }
}
