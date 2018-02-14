import { ModuleWithProviders, NgModule, Optional, SkipSelf } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';

import * as SYNDESIS_ABSTRACT_PROVIDERS from '@syndesis/ui/platform';
import * as SYNDESIS_PROVIDERS from './providers';

@NgModule({
  imports: [CommonModule, HttpClientModule],
})
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error('CoreModule is already loaded. Import it in the AppModule only');
    }
  }

  static forRoot(): Array<ModuleWithProviders> {
    return [{
      ngModule: CoreModule,
      providers: [
        SYNDESIS_PROVIDERS.IntegrationProviderService,
        {
          provide: SYNDESIS_ABSTRACT_PROVIDERS.IntegrationService,
          useClass: SYNDESIS_PROVIDERS.IntegrationProviderService
        },
        SYNDESIS_PROVIDERS.FormFactoryProviderService,
        {
          provide: SYNDESIS_ABSTRACT_PROVIDERS.FormFactoryService,
          useClass: SYNDESIS_PROVIDERS.FormFactoryProviderService,
        },
        SYNDESIS_PROVIDERS.UserProviderService,
        {
          provide: SYNDESIS_ABSTRACT_PROVIDERS.UserService,
          useClass: SYNDESIS_PROVIDERS.UserProviderService
        },
        SYNDESIS_PROVIDERS.IntegrationSupportProviderService,
        {
          provide: SYNDESIS_ABSTRACT_PROVIDERS.IntegrationSupportService,
          useClass: SYNDESIS_PROVIDERS.IntegrationSupportProviderService
        }
      ]},
    ];
  }
}
