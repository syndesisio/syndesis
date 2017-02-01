import { BrowserModule } from '@angular/platform-browser';
import { NgModule, APP_INITIALIZER } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { RestangularModule } from 'ng2-restangular';
import { OAuthService, OAuthModule } from 'angular-oauth2-oidc';

import { AppRoutingModule } from './approuting/approuting.module';
import { StoreModule } from './store/store.module';
import { IPaaSCommonModule } from './common/common.module';

import { AppComponent } from './app.component';
import { ConfigService, configServiceInitializer } from './config.service';

export function restangularProviderConfigurer(restangularProvider: any, config: ConfigService, oauthService: OAuthService) {
  restangularProvider.setBaseUrl(config.getSettings().apiEndpoint);

  restangularProvider.addFullRequestInterceptor((_element, _operation, _path, _url, headers) => {
    return {
      headers: Object.assign({}, headers, { Authorization: 'Bearer ' + oauthService.getAccessToken() }),
    };
  });

  restangularProvider.addResponseInterceptor((data: any, operation: string) => {
    if (operation === 'getList' && Array.isArray(data.items)) {
      const pagingData = data.items;
      if (!!pagingData.totalCount) {
        pagingData.totalCount = data.totalCount;
      } else {
        pagingData.totalCount = pagingData.length;
      }
      return pagingData;
    }
    return data;
  });
}

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    RestangularModule.forRoot([ConfigService, OAuthService], restangularProviderConfigurer),
    NgbModule.forRoot(),
    AppRoutingModule,
    StoreModule,
    IPaaSCommonModule.forRoot(),
    OAuthModule.forRoot(),
  ],
  providers: [
    ConfigService,
    {
      provide: APP_INITIALIZER,
      useFactory: configServiceInitializer,
      deps: [ConfigService],
      multi: true,
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule { }
