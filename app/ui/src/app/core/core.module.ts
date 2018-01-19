import { ModuleWithProviders, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';

import { ApiHttpService, ApiConfigService, API_ENDPOINTS } from '@syndesis/ui/platform';
import * as SYNDESIS_PROVIDERS from './providers';

@NgModule({
  imports: [CommonModule, HttpClientModule],
})
export class CoreModule {
  static forRoot(): Array<ModuleWithProviders> {
    return [{
      ngModule: CoreModule,
      providers: [
        SYNDESIS_PROVIDERS.ApiConfigProviderService,
        SYNDESIS_PROVIDERS.ApiHttpProviderService,
        {
          provide: API_ENDPOINTS,
          multi: true,
          useValue: {}
        }, {
          provide: ApiConfigService,
          useClass: SYNDESIS_PROVIDERS.ApiConfigProviderService
        }, {
          provide: ApiHttpService,
          useClass: SYNDESIS_PROVIDERS.ApiHttpProviderService
        },
      ]},
    ];
  }

  static forFeature({ apiConfig }): Array<ModuleWithProviders> {
    const featureProviders = [];

    if (apiConfig) {
      featureProviders.push({
        provide: API_ENDPOINTS,
        multi: true,
        useValue: apiConfig
      }, {
        provide: ApiConfigService,
        useClass: SYNDESIS_PROVIDERS.ApiConfigProviderService
      }, {
        provide: ApiHttpService,
        useClass: SYNDESIS_PROVIDERS.ApiHttpProviderService
      });
    }
    return [{
      ngModule: CoreModule,
      providers: featureProviders
    }];
  }
}
