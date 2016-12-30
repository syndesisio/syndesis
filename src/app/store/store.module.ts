import { NgModule, Optional, SkipSelf } from '@angular/core';

import { RestangularModule } from 'ng2-restangular';

import { IntegrationsService } from './model/integrations.service';

@NgModule({
  imports: [
    RestangularModule
  ],
  providers: [
    IntegrationsService
  ],
  declarations: []
})
export class StoreModule {

  constructor( @Optional() @SkipSelf() parentModule: StoreModule) {
    if (parentModule) {
      throw new Error('StoreModule is already loaded. Import it in the AppModule only');
    }
  }

}
