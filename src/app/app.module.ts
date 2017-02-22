import { BrowserModule } from '@angular/platform-browser';
import { NgModule, APP_INITIALIZER } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { RestangularModule } from 'ng2-restangular';
import { OAuthService, OAuthModule } from 'angular-oauth2-oidc-hybrid';
import { DynamicFormsCoreModule } from '@ng2-dynamic-forms/core';
import { DynamicFormsBootstrapUIModule } from '@ng2-dynamic-forms/ui-bootstrap';
import { Observable } from 'rxjs/Observable';

import { TabsModule, ModalModule, DropdownModule, CollapseModule, AlertModule } from 'ng2-bootstrap';
import { ToasterModule, ToasterService } from 'angular2-toaster';

import { AppRoutingModule } from './approuting/approuting.module';
import { StoreModule } from './store/store.module';
import { IPaaSCommonModule } from './common/common.module';

import { AppComponent } from './app.component';
import { ConfigService } from './config.service';
import { UserService } from './common/user.service';
import { log } from './logging';

export function appInitializer(configService: ConfigService, oauthService: OAuthService, userService: UserService) {
  return () => {
    return configService.load().then(() => {
      // URL of the SPA to redirect the user to after login
      oauthService.redirectUri = window.location.origin + '/dashboard';
      oauthService.clientId = configService.getSettings('oauth', 'clientId');
      oauthService.scope = (configService.getSettings('oauth', 'scopes') as string[]).join(' ');
      oauthService.oidc = configService.getSettings('oauth', 'oidc');
      oauthService.hybrid = configService.getSettings('oauth', 'hybrid');
      oauthService.setStorage(sessionStorage);
      oauthService.issuer = configService.getSettings('oauth', 'issuer');

      return oauthService.loadDiscoveryDocument();
    }).then(() => {
      if (!oauthService.hasValidAccessToken()) {
        if (!oauthService.tryLogin({})) {
          return oauthService.initImplicitFlow();
        }
      }
      oauthService.loadUserProfile().then(() => {
        userService.setUser(oauthService.getIdentityClaims());

        Observable.interval(1000 * 60).subscribe(
          () => {
            oauthService.refreshToken().catch(
              (reason) => log.errorc(() => 'Failed to refresh token', () => new Error(reason)),
            );
          },
        );
      });
    });
  };
}

export function restangularProviderConfigurer(restangularProvider: any, config: ConfigService, oauthService: OAuthService) {
  restangularProvider.setBaseUrl(config.getSettings().apiEndpoint);

  restangularProvider.addFullRequestInterceptor((_element, _operation, _path, _url, headers) => {
    const accessToken = oauthService.getAccessToken();
    return {
      headers: Object.assign({}, headers, { Authorization: 'Bearer ' + accessToken }),
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
    ReactiveFormsModule,
    HttpModule,
    DynamicFormsCoreModule.forRoot(),
    DynamicFormsBootstrapUIModule,
    RestangularModule.forRoot([ConfigService, OAuthService], restangularProviderConfigurer),
    TabsModule.forRoot(),
    ModalModule.forRoot(),
    DropdownModule.forRoot(),
    CollapseModule.forRoot(),
    AlertModule.forRoot(),
    AppRoutingModule,
    StoreModule,
    IPaaSCommonModule.forRoot(),
    OAuthModule.forRoot(),
    ToasterModule,
  ],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: appInitializer,
      deps: [ConfigService, OAuthService, UserService],
      multi: true,
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule { }
