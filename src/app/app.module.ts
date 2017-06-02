import { BrowserModule } from '@angular/platform-browser';
import { NgModule, NgZone, APP_INITIALIZER } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { RestangularModule } from 'ngx-restangular';
import { OAuthService, OAuthModule } from 'angular-oauth2-oidc-hybrid';
import { DynamicFormsCoreModule } from '@ng2-dynamic-forms/core';
import { DynamicFormsBootstrapUIModule } from '@ng2-dynamic-forms/ui-bootstrap';
import { Observable } from 'rxjs/Observable';

import { TabsModule,
         ModalModule,
         BsDropdownModule,
         CollapseModule,
         AlertModule,
         PopoverModule,
         TooltipModule,
         TypeaheadModule } from 'ngx-bootstrap';
import { ToasterModule, ToasterService } from 'angular2-toaster';

import { AppRoutingModule } from './approuting/approuting.module';
import { StoreModule } from './store/store.module';
import { SyndesisCommonModule } from './common/common.module';

import { AppComponent } from './app.component';
import { ConfigService } from './config.service';
import { UserService } from './common/user.service';
import { log } from './logging';

import { DataMapperModule } from 'syndesis.data.mapper';

export function appInitializer(configService: ConfigService, oauthService: OAuthService, userService: UserService, ngZone: NgZone) {
  return () => {
    return configService.load().then(() => {
      oauthService.clientId = configService.getSettings('oauth', 'clientId');
      oauthService.scope = (configService.getSettings('oauth', 'scopes') as string[]).join(' ');
      oauthService.oidc = configService.getSettings('oauth', 'oidc');
      oauthService.hybrid = configService.getSettings('oauth', 'hybrid');
      oauthService.setStorage(sessionStorage);
      oauthService.issuer = configService.getSettings('oauth', 'issuer');

      return oauthService.loadDiscoveryDocument();
    }).then(() => {
      // If we don't have a valid token, then let's try to get one. This means we haven't logged in at all.
      if (!oauthService.hasValidAccessToken()) {
        // Let's get the current location for the redirect URI.
        let currentLocation = window.location.href;
        const hashIndex = currentLocation.indexOf('#');
        if (hashIndex > 0) {
           currentLocation = currentLocation.substring(0, hashIndex);
        }
        oauthService.redirectUri = currentLocation;

        // If this is the first flow, authenticating against OpenShift, then we shouldn't
        // do the hybrid flow. Let's store whether this is the first IDP in session storage
        // so it survives the multiple required redirects.
        let firstIDP = sessionStorage.getItem('syndesis-first-idp');
        if (!firstIDP) {
          firstIDP = 'true';
          sessionStorage.setItem('syndesis-first-idp', firstIDP);
        }

        // Store whether the oidc client was configured as hybrid so we can enable token refreshes.
        const originalHybrid = oauthService.hybrid;
        oauthService.hybrid = oauthService.hybrid && firstIDP !== 'true';

        // Before we kick off the implicit flow, we should check that this isn't a redirect back from the auth server
        // and the token isn't present in the location hash - tryLogin does that.
        if (!oauthService.tryLogin()) {
          // There is no token stored or in location hash so kick off implicit flow.
          return oauthService.initImplicitFlow();
        }

        // Set this back so that second flow through we do the proper code flow to get a refresh token.
        sessionStorage.setItem('syndesis-first-idp', 'false');
        oauthService.hybrid = originalHybrid;

        let autoLinkGithHub = configService.getSettings('oauth')['auto-link-github'];
        if (autoLinkGithHub === undefined) {
          autoLinkGithHub = true;
        }

        // If this wasn't the autolink flow then rekick off flow with state set to autolink.
        if (autoLinkGithHub && oauthService.state !== 'autolink') {
          // Client suggested IDP works great with Keycloak.
          oauthService.loginUrl += '?kc_idp_hint=github';
          // Clear session storage before trying again.
          oauthService.logOut(true);
          // And kick off the login flow again.
          return oauthService.initImplicitFlow('autolink');
        }
      }

      // Remove this marker from session storage as it has served it's purpose.
      sessionStorage.removeItem('syndesis-first-idp');

      // Use the token to load our user details and set up the refresh token flow.
      oauthService.loadUserProfile().then(() => {
        userService.setUser(oauthService.getIdentityClaims());

        // Only do refreshes if we're doing a hybrid oauth flow.
        if (oauthService.hybrid) {
          ngZone.runOutsideAngular(() => {
            // see https://christianliebel.com/2016/11/angular-2-protractor-timeout-heres-fix/
            // registered observable / timeout makes protractor wait forever
            Observable.interval(1000 * 60).subscribe(
              () => {
                ngZone.run(() => {
                  oauthService.refreshToken().catch(
                    (reason) => log.errorc(() => 'Failed to refresh token', () => new Error(reason)),
                  );
                });
              },
            );
          });
        }
      });
    });
  };
}

export function restangularProviderConfigurer(restangularProvider: any, config: ConfigService, oauthService: OAuthService) {
  restangularProvider.setPlainByDefault(true);
  restangularProvider.setBaseUrl(config.getSettings().apiEndpoint);

  restangularProvider.addFullRequestInterceptor((_element, _operation, _path, _url, headers) => {
    const accessToken = oauthService.getAccessToken();
    return {
      headers: Object.assign({}, headers, { Authorization: 'Bearer ' + accessToken }),
    };
  });

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
    TooltipModule.forRoot(),
    ModalModule.forRoot(),
    BsDropdownModule.forRoot(),
    CollapseModule.forRoot(),
    AlertModule.forRoot(),
    PopoverModule.forRoot(),
    TypeaheadModule.forRoot(),
    AppRoutingModule,
    StoreModule,
    SyndesisCommonModule.forRoot(),
    OAuthModule.forRoot(),
    ToasterModule,
    DataMapperModule,
  ],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: appInitializer,
      deps: [ConfigService, OAuthService, UserService, NgZone],
      multi: true,
    },
    ConfigService,
    OAuthService,
    UserService,
  ],
  bootstrap: [AppComponent],
})
export class AppModule { }
