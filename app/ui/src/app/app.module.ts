import { APP_INITIALIZER, NgModule, InjectionToken } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import { DynamicFormsCoreModule } from '@ng-dynamic-forms/core';
import { VendorModule } from '@syndesis/ui/vendor';
import { TagInputModule } from 'ngx-chips';
import { NotificationModule } from 'patternfly-ng';
import { DataMapperModule } from '@atlasmap/atlasmap.data.mapper';

import { ApiModule } from './api';
import { CoreModule } from './core';
import { environment } from '../environments/environment';
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app.routing';
import { SyndesisCommonModule } from './common';
import { appConfigInitializer, ConfigService } from './config.service';
import { StoreModule as LegacyStoreModule } from './store/store.module';
import { platformReducer, PlatformEffects, platformEndpoints, SYNDESIS_GUARDS } from './platform';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    ApiModule.forRoot(platformEndpoints),
    CoreModule.forRoot(),
    DynamicFormsCoreModule.forRoot(),
    VendorModule,
    TagInputModule,
    AppRoutingModule,
    LegacyStoreModule,
    StoreModule.forRoot(platformReducer),
    EffectsModule.forRoot(PlatformEffects.rootEffects()),
    !environment.production ? StoreDevtoolsModule.instrument({ maxAge: 25 }) : [],
    SyndesisCommonModule.forRoot(),
    DataMapperModule,
    NotificationModule,
  ],
  providers: [
    ...SYNDESIS_GUARDS,
    ConfigService,
    {
      provide: APP_INITIALIZER,
      useFactory: appConfigInitializer,
      deps: [ConfigService],
      multi: true
    },
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
