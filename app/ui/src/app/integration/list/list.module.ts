import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActionModule, ListModule } from 'patternfly-ng';
import { TooltipModule } from 'ngx-bootstrap/tooltip';

import { SyndesisCommonModule } from '@syndesis/ui/common';
import { IntegrationSupportModule } from '../integration-support.module';
import { IntegrationStatusComponent } from '../components';
import { IntegrationListComponent } from './list.component';

@NgModule({
  imports: [
    ActionModule,
    CommonModule,
    ListModule,
    SyndesisCommonModule,
    IntegrationSupportModule,
    TooltipModule
  ],
  declarations: [IntegrationStatusComponent, IntegrationListComponent],
  exports: [IntegrationListComponent, IntegrationStatusComponent]
})
export class IntegrationListModule {}
