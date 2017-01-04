import { NgModule, Optional, SkipSelf } from '@angular/core';

import { StoreModule as NgRxStoreModule } from '@ngrx/store';
import { RouterStoreModule } from '@ngrx/router-store';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import { RestangularModule } from 'ng2-restangular';

import { reducers } from './store';
import { IntegrationService } from './integration/integration.service';
import { TemplateService } from './template/template.service';
import { ConnectionService } from './connection/connection.service';

@NgModule({
  imports: [
    RestangularModule,
    NgRxStoreModule.provideStore(reducers),
    RouterStoreModule.connectRouter(),
    StoreDevtoolsModule.instrumentOnlyWithExtension(),
  ],
  providers: [
    IntegrationService,
    TemplateService,
    ConnectionService,
  ],
})
export class StoreModule {

  constructor( @Optional() @SkipSelf() parentModule: StoreModule) {
    if (parentModule) {
      throw new Error('StoreModule is already loaded. Import it in the AppModule only');
    }
  }

}
