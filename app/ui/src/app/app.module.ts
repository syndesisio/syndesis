import { APP_INITIALIZER, NgModule, InjectionToken } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import { DynamicFormsCoreModule } from '@ng-dynamic-forms/core';
import { DynamicFormsBootstrapUIModule } from '@ng-dynamic-forms/ui-bootstrap';
import {
  AlertModule,
  CollapseModule,
  ModalModule,
  PopoverModule,
  TabsModule,
  TooltipModule,
  TypeaheadModule
} from 'ngx-bootstrap';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TagInputModule } from 'ngx-chips';
import { Restangular, RestangularModule } from 'ngx-restangular';
import { TourNgxBootstrapModule } from 'ngx-tour-ngx-bootstrap';
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

export function restangularProviderConfigurer(
  restangularProvider: any,
  config: ConfigService
) {
  restangularProvider.setPlainByDefault(true);
  restangularProvider.setBaseUrl(config.getSettings().apiEndpoint);

  restangularProvider.addResponseInterceptor((data: any, operation: string) => {
    switch (operation) {
      case 'getList':
        if (!Array.isArray(data)) {
          data = data.items || [];
        }
        break;
      default:
    }
    return data;
  });
}

export const RESTANGULAR_MAPPER = new InjectionToken<Restangular>(
  'restangularMapper'
);
export function mapperRestangularProvider(
  restangular: Restangular,
  config: ConfigService
) {
  return restangular.withConfig(restangularConfigurer => {
    const mapperEndpoint = config.getSettings().mapperEndpoint;
    restangularConfigurer.setBaseUrl(
      mapperEndpoint ? mapperEndpoint : '/mapper/v1'
    );
  });
}

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpModule,
    ApiModule.forRoot(platformEndpoints),
    CoreModule.forRoot(),
    DynamicFormsCoreModule.forRoot(),
    DynamicFormsBootstrapUIModule,
    RestangularModule.forRoot([ConfigService], restangularProviderConfigurer),
    TabsModule.forRoot(),
    TooltipModule.forRoot(),
    ModalModule.forRoot(),
    BsDropdownModule.forRoot(),
    CollapseModule.forRoot(),
    AlertModule.forRoot(),
    PopoverModule.forRoot(),
    TypeaheadModule.forRoot(),
    TagInputModule,
    AppRoutingModule,
    LegacyStoreModule,
    StoreModule.forRoot(platformReducer),
    EffectsModule.forRoot(PlatformEffects.rootEffects()),
    !environment.production ? StoreDevtoolsModule.instrument({ maxAge: 25 }) : [],
    SyndesisCommonModule.forRoot(),
    DataMapperModule,
    NotificationModule,
    TourNgxBootstrapModule.forRoot()
  ],
  providers: [
    ...SYNDESIS_GUARDS,
    {
      provide: APP_INITIALIZER,
      useFactory: appConfigInitializer,
      deps: [ConfigService],
      multi: true
    },
    {
      provide: RESTANGULAR_MAPPER,
      useFactory: mapperRestangularProvider,
      deps: [Restangular, ConfigService]
    },
    ConfigService
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
