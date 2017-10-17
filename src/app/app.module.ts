import { APP_INITIALIZER, NgModule, NgZone } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DynamicFormsCoreModule } from '@ng2-dynamic-forms/core';
import {
  AlertModule,
  BsDropdownModule,
  CollapseModule,
  ModalModule,
  PopoverModule,
  TabsModule,
  TooltipModule,
  TypeaheadModule,
} from 'ngx-bootstrap';
import { TagInputModule } from 'ngx-chips';
import { RestangularModule } from 'ngx-restangular';
import { NotificationModule, NotificationService } from 'patternfly-ng';
import { DataMapperModule } from 'syndesis.data.mapper';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './approuting/approuting.module';
import { CanDeactivateGuard } from './common/can-deactivate-guard.service';
import { SyndesisCommonModule } from './common/common.module';
import { UserService } from './common/user.service';
import { ConfigService } from './config.service';
import { StoreModule } from './store/store.module';

export function appInitializer(
  configService: ConfigService,
  userService: UserService,
  ngZone: NgZone,
  notificationService: NotificationService,
) {
  return () => {
    return configService.load();
  };
}

export function restangularProviderConfigurer(
  restangularProvider: any,
  config: ConfigService,
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

/**
 * The main module of this library. Example usage:
 *
 * ```typescript
 * import { AppModule } from 'syndesis-ui';
 *
 * &commat;NgModule({
 *   imports: [
 *     AppModule.forRoot()
 *   ]
 * })
 * class AppModule {}
 * ```
 *
 */
@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    HttpModule,
    DynamicFormsCoreModule.forRoot(),
    RestangularModule.forRoot(
      [ConfigService],
      restangularProviderConfigurer,
    ),
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
    StoreModule,
    SyndesisCommonModule.forRoot(),
    DataMapperModule,
    NotificationModule,
  ],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: appInitializer,
      deps: [ConfigService, UserService, NgZone, NotificationService],
      multi: true,
    },
    ConfigService,
    UserService,
    CanDeactivateGuard,
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
