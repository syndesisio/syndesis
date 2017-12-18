import { APP_INITIALIZER, NgModule, InjectionToken } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { StoreModule } from '@ngrx/store';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import { DynamicFormsCoreModule } from '@ng-dynamic-forms/core';
import {
  AlertModule,
  BsDropdownModule,
  CollapseModule,
  ModalModule,
  PopoverModule,
  TabsModule,
  TooltipModule,
  TypeaheadModule
} from 'ngx-bootstrap';
import { TagInputModule } from 'ngx-chips';
import { Restangular, RestangularModule } from 'ngx-restangular';
import { TourNgxBootstrapModule } from 'ngx-tour-ngx-bootstrap';
import { NotificationModule } from 'patternfly-ng';
import { DataMapperModule } from '@atlasmap/atlasmap.data.mapper';

import { environment } from '../environments/environment';
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app.routing';
import { CanDeactivateGuard, SyndesisCommonModule } from './common';
import { UserService } from './common/user.service';
import { ConfigService } from './config.service';
import { StoreModule as LegacyStoreModule } from './store/store.module';
import { platformReducer } from './platform';

export function appInitializer(configService: ConfigService) {
  return () => {
    return configService.load();
  };
}

export function restangularProviderConfigurer(
  restangularProvider: any,
  config: ConfigService
) {
  restangularProvider.setPlainByDefault(true);
  restangularProvider.setBaseUrl(config.getSettings().apiEndpoint);

  restangularProvider.addResponseInterceptor((data: any, operation: string) => {
    if (operation === 'getList' && data && Array.isArray(data.items)) {
      const pagingData = data.items;
      if (!!pagingData.totalCount) {
        pagingData.totalCount = data.totalCount;
      } else {
        pagingData.totalCount = pagingData.length;
      }
      return pagingData;
    }
    if (!data) {
      return [];
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
    DynamicFormsCoreModule.forRoot(),
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
    !environment.production ? StoreDevtoolsModule.instrument({ maxAge: 25 }) : [],
    SyndesisCommonModule.forRoot(),
    DataMapperModule,
    NotificationModule,
    TourNgxBootstrapModule.forRoot()
  ],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: appInitializer,
      deps: [ConfigService],
      multi: true
    },
    {
      provide: RESTANGULAR_MAPPER,
      useFactory: mapperRestangularProvider,
      deps: [Restangular, ConfigService]
    },
    ConfigService,
    UserService,
    CanDeactivateGuard
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
