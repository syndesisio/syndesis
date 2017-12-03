import { NgModule, Optional, SkipSelf } from '@angular/core';
import { HttpModule } from '@angular/http';
import { RestangularModule } from 'ngx-restangular';

import { ActionService } from './action/action.service';
import { ActionStore } from './action/action.store';
import { ConnectionService } from './connection/connection.service';
import { ConnectionStore } from './connection/connection.store';
import { ConnectorService } from './connector/connector.service';
import { ConnectorStore } from './connector/connector.store';
import { EventsService } from './entity/events.service';
import { ExtensionService } from './extension/extension.service';
import { ExtensionStore } from './extension/extension.store';
import { IntegrationSupportService } from './integration-support.service';
import { IntegrationService } from './integration/integration.service';
import { IntegrationStore } from './integration/integration.store';
import { OAuthAppService } from './oauthApp/oauth-app.service';
import { OAuthAppStore } from './oauthApp/oauth-app.store';
import { StepStore } from './step/step.store';
import { TemplateService } from './template/template.service';
import { TemplateStore } from './template/template.store';
import { TestSupportService } from './test-support.service';

@NgModule({
  imports: [HttpModule, RestangularModule],
  providers: [
    ActionService,
    ConnectionService,
    ConnectorService,
    IntegrationService,
    IntegrationSupportService,
    TemplateService,
    EventsService,
    ExtensionService,
    ExtensionStore,
    ActionStore,
    ConnectionStore,
    ConnectorStore,
    IntegrationStore,
    TemplateStore,
    TestSupportService,
    StepStore,
    OAuthAppService,
    OAuthAppStore
  ]
})
export class StoreModule {
  constructor(
    @Optional()
    @SkipSelf()
    parentModule: StoreModule
  ) {
    if (parentModule) {
      throw new Error(
        'StoreModule is already loaded. Import it in the AppModule only'
      );
    }
  }
}
