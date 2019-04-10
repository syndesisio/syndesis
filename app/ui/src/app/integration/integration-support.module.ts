import { NgModule } from '@angular/core';
import * as SYNDESIS_ABSTRACT_PROVIDERS from '@syndesis/ui/platform';

import {
  IntegrationActionsProviderService
} from '@syndesis/ui/integration/integration-actions-provider.service';

// TODO: This module is imported several times through the app, so it needs to
//       expose forRoot() and forChild() methods. Otherwise it will wind up
//       triggering the execution of ApiModule.forChild() on each import.
@NgModule({
  providers: [
    IntegrationActionsProviderService,
    {
      provide: SYNDESIS_ABSTRACT_PROVIDERS.IntegrationActionsService,
      useClass: IntegrationActionsProviderService
    }
  ]
})
export class IntegrationSupportModule {}
