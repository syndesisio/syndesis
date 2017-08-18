import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActionModule, ListModule } from 'patternfly-ng';
import { ModalModule } from 'ngx-bootstrap';
import { TooltipModule } from 'ngx-bootstrap/tooltip';

import { IntegrationStatusComponent } from '../components/status.component';
import { IntegrationsListComponent } from './list.component';

@NgModule({
  imports: [
    ActionModule,
    CommonModule,
    ListModule,
    ModalModule,
    TooltipModule,
  ],
  declarations: [
    IntegrationStatusComponent,
    IntegrationsListComponent,
  ],
  exports: [
    IntegrationsListComponent,
    IntegrationStatusComponent,
  ],
})
export class IntegrationsListModule {
}
