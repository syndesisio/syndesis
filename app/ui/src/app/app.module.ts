import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { DynamicFormsCoreModule } from '@ng-dynamic-forms/core';
import { VendorModule } from '@syndesis/ui/vendor';
import { TagInputModule } from 'ngx-chips';
import { ClickOutsideModule } from 'ng-click-outside';
import { ToastNotificationListModule as NotificationModule } from 'patternfly-ng';
import { DataMapperModule } from '@atlasmap/atlasmap-data-mapper';

import { ApiModule } from '@syndesis/ui/api';
import { CoreModule } from '@syndesis/ui/core';
import { AppComponent } from '@syndesis/ui/app.component';
import { AppRoutingModule } from '@syndesis/ui/app.routing';
import { SyndesisCommonModule } from '@syndesis/ui/common';
import { appConfigInitializer, ConfigService } from '@syndesis/ui/config.service';
import { SyndesisStoreModule } from '@syndesis/ui/store/store.module';
import { platformEndpoints, PlatformModule } from '@syndesis/ui/platform';
import { ERROR_HANDLER_PROVIDERS } from '@syndesis/ui/error-handler';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    ClickOutsideModule,
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
