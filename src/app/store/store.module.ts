import { NgModule, Optional, SkipSelf } from '@angular/core';
import { HttpModule } from '@angular/http';

import { RestangularModule } from 'ngx-restangular';

import { OAuthModule } from 'angular-oauth2-oidc-hybrid';

import { ActionService } from './action/action.service';
import { ConnectionService } from './connection/connection.service';
import { ConnectorService } from './connector/connector.service';
import { EventsService } from './entity/events.service';
import { IntegrationService } from './integration/integration.service';
import { TemplateService } from './template/template.service';
import { IntegrationSupportService } from './integration-support.service';
import { TestSupportService } from './test-support.service';

import { ActionStore } from './action/action.store';
import { ConnectionStore } from './connection/connection.store';
import { ConnectorStore } from './connector/connector.store';
import { IntegrationStore } from './integration/integration.store';
import { TemplateStore } from './template/template.store';
import { StepStore } from './step/step.store';

@NgModule({
  imports: [
    HttpModule,
    RestangularModule,
    OAuthModule,
  ],
  providers: [
    ActionService,
    ConnectionService,
    ConnectorService,
    IntegrationService,
    IntegrationSupportService,
    TemplateService,
    EventsService,
    ActionStore,
    ConnectionStore,
    ConnectorStore,
    IntegrationStore,
    TemplateStore,
    TestSupportService,
    StepStore,
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
