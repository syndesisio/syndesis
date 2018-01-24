import { NgModule } from '@angular/core';
import { integrationSupportEndpoints } from './integration-support.api';
import { IntegrationSupportService } from './integration-support.service';
import { ApiModule } from '@syndesis/ui/api';

@NgModule({
  imports: [
    ApiModule.forChild(integrationSupportEndpoints)
  ],
  providers: [
    IntegrationSupportService
  ]
})
export class IntegrationSupportModule {}
