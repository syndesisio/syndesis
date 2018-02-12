import { NgModule } from '@angular/core';
import { integrationSupportEndpoints } from './integration-support.api';
import * as SYNDESIS_ABSTRACT_PROVIDERS from '@syndesis/ui/platform';

import { IntegrationSupportProviderService } from './integration-support-provider.service';
import { IntegrationActionsProviderService } from './integration-actions-provider.service';
import { ApiModule } from '@syndesis/ui/api';

// TODO: This module is imported several times through the app, so it needs to
//       expose forRoot() and forChild() methods. Otherwise it will wind up
//       triggering the execution of ApiModule.forChild() on each import.
@NgModule({
  imports: [
    ApiModule.forChild(integrationSupportEndpoints)
  ],
  providers: [
    IntegrationActionsProviderService,
    {
      provide: SYNDESIS_ABSTRACT_PROVIDERS.IntegrationActionsService,
      useClass: IntegrationActionsProviderService
    },
    IntegrationSupportProviderService,
    {
      provide: SYNDESIS_ABSTRACT_PROVIDERS.IntegrationSupportService,
      useClass: IntegrationSupportProviderService
    }
  ]
})
export class IntegrationSupportModule {}
