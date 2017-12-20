import { ModuleWithProviders, NgModule, Optional, SkipSelf } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';

import { ApiHttpService } from '@syndesis/ui/platform';
import * as SYNDESIS_PROVIDERS from './providers';

@NgModule({
  imports: [CommonModule, HttpClientModule],
})
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error('Syndesis CoreModule is already loaded. Import it in the AppModule only');
    }
  }

  static forRoot(): Array<ModuleWithProviders> {
    return [{
      ngModule: CoreModule,
      providers: [
        SYNDESIS_PROVIDERS.ApiHttpProviderService,
        {
          provide: ApiHttpService,
          useClass: SYNDESIS_PROVIDERS.ApiHttpProviderService
        },
      ]},
    ];
  }
}
