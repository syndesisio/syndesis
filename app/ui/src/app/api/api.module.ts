import { ModuleWithProviders, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule, HTTP_INTERCEPTORS, HttpClientXsrfModule } from '@angular/common/http';

import { environment } from 'environments/environment';
import { ApiHttpService, ApiConfigService, Endpoints, API_ENDPOINTS } from '@syndesis/ui/platform';
import * as SYNDESIS_API_PROVIDERS from '@syndesis/ui/api/providers';

export function endpointsLazyLoaderFactory(apiEndpoints: Endpoints, apiConfigService: ApiConfigService) {
  return new SYNDESIS_API_PROVIDERS.ApiEndpointsLazyLoaderService(apiEndpoints, apiConfigService);
}

@NgModule({
  imports: [
    CommonModule,
    HttpClientModule,
    HttpClientXsrfModule.withOptions(environment.xsrf)
  ],
})
export class ApiModule {
  static forRoot(apiEndpoints?: Endpoints): Array<ModuleWithProviders> {
    return [{
      ngModule: ApiModule,
      providers: [{
        provide: API_ENDPOINTS,
        multi: true,
        useValue: apiEndpoints || {}
      },
      SYNDESIS_API_PROVIDERS.ApiConfigProviderService,
      {
        provide: ApiConfigService,
        useClass: SYNDESIS_API_PROVIDERS.ApiConfigProviderService
      },
      SYNDESIS_API_PROVIDERS.ApiHttpProviderService,
      {
        provide: ApiHttpService,
        useClass: SYNDESIS_API_PROVIDERS.ApiHttpProviderService
      },
      {
        provide: HTTP_INTERCEPTORS,
        useClass: SYNDESIS_API_PROVIDERS.ApiHttpInterceptor,
        multi: true
      },
      {
        provide: HTTP_INTERCEPTORS,
        useClass: SYNDESIS_API_PROVIDERS.ApiXsrfInterceptor,
        multi: true
      }
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
      SYNDESIS_API_PROVIDERS.ApiEndpointsLazyLoaderService,
      {
        provide: SYNDESIS_API_PROVIDERS.ApiEndpointsLazyLoaderService,
        useFactory: endpointsLazyLoaderFactory,
        deps: [API_ENDPOINTS, ApiConfigService]
      }]
    }];
  }
}
