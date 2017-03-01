import { NgModule, Optional, SkipSelf } from '@angular/core';

import { RestangularModule } from 'ng2-restangular';

import { IntegrationService } from './integration/integration.service';
import { TemplateService } from './template/template.service';
import { ConnectionService } from './connection/connection.service';
import { IntegrationStore } from './integration/integration.store';
import { TemplateStore } from './template/template.store';
import { ConnectionStore } from './connection/connection.store';
import { ConnectorStore } from './connector/connector.store';
import { ConnectorService } from './connector/connector.service';
import { EventsService } from './entity/events.service';

@NgModule({
  imports: [
    RestangularModule,
  ],
  providers: [
    IntegrationService,
    TemplateService,
    EventsService,
    ConnectionService,
    ConnectorService,
    IntegrationStore,
    TemplateStore,
    ConnectionStore,
    ConnectorStore,
  ],
})
export class StoreModule {
  constructor( @Optional() @SkipSelf() parentModule: StoreModule) {
    if (parentModule) {
      throw new Error(
        'StoreModule is already loaded. Import it in the AppModule only');
    }
  }
}
