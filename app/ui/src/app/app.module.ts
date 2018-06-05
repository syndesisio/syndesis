import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { DynamicFormsCoreModule } from '@ng-dynamic-forms/core';
import { VendorModule } from '@syndesis/ui/vendor';
import { TagInputModule } from 'ngx-chips';
import { NotificationModule } from 'patternfly-ng';
import { DataMapperModule } from '@atlasmap/atlasmap-data-mapper';

import { ApiModule } from './api';
import { CoreModule } from './core';
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app.routing';
import { SyndesisCommonModule } from './common';
import { appConfigInitializer, ConfigService } from './config.service';
import { SyndesisStoreModule } from './store/store.module';
import { platformEndpoints, PlatformModule } from './platform';
import { ERROR_HANDLER_PROVIDERS } from './error-handler';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    SyndesisCommonModule.forRoot(),
    ApiModule.forRoot(platformEndpoints),
    CoreModule.forRoot(),
    PlatformModule.forRoot(),
    DynamicFormsCoreModule.forRoot(),
    VendorModule,
    TagInputModule,
    AppRoutingModule,
    SyndesisStoreModule,
    DataMapperModule,
    NotificationModule
  ],
  providers: [
    ERROR_HANDLER_PROVIDERS,
    ConfigService,
    {
      provide: APP_INITIALIZER,
      useFactory: appConfigInitializer,
      deps: [ConfigService],
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
