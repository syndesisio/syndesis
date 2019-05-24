import { NgModule, Optional, SkipSelf } from '@angular/core';

import { ActionService } from '@syndesis/ui/store/action/action.service';
import { ActionStore } from '@syndesis/ui/store/action/action.store';
import { ConnectionService } from '@syndesis/ui/store/connection/connection.service';
import { ConnectionStore } from '@syndesis/ui/store/connection/connection.store';
import { ConnectorService, ConnectorStore } from '@syndesis/ui/store/connector';
import { EventsService } from '@syndesis/ui/store/entity/events.service';
import { ExtensionService } from '@syndesis/ui/store/extension/extension.service';
import { ExtensionStore } from '@syndesis/ui/store/extension/extension.store';
import { IntegrationService } from '@syndesis/ui/store/integration/integration.service';
import { IntegrationStore } from '@syndesis/ui/store/integration/integration.store';
import { OAuthAppService } from '@syndesis/ui/store/oauthApp/oauth-app.service';
import { OAuthAppStore } from '@syndesis/ui/store/oauthApp/oauth-app.store';
import { StepStore } from '@syndesis/ui/store/step/step.store';
import { TestSupportService } from '@syndesis/ui/store/test-support.service';

@NgModule({
  providers: [
    EventsService,
    ActionService,
    ConnectionService,
    ConnectorService,
    IntegrationService,
    ExtensionService,
    ExtensionStore,
    ActionStore,
    ConnectionStore,
    ConnectorStore,
    IntegrationStore,
    TestSupportService,
    StepStore,
    OAuthAppService,
    OAuthAppStore
  ]
})
export class SyndesisStoreModule {
  constructor(@Optional() @SkipSelf() parentModule: SyndesisStoreModule) {
    if (parentModule) {
      throw new Error(
        'SyndesisStoreModule is already loaded. Import it in the AppModule only'
      );
    }
  }
}
