import { Component, ChangeDetectionStrategy, OnInit, AfterViewInit } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';

import { ConfigService } from './config.service';
import { UserService } from './common/user.service';

@Component({
  selector: 'ipaas-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppComponent implements OnInit, AfterViewInit {
  name = 'Red Hat iPaaS';

  // White BG
  logoWhiteBg = 'assets/images/rh_ipaas_small.svg';
  iconWhiteBg = 'assets/images/glasses_logo.svg';

  // Dark BG
  logoDarkBg = 'assets/images/rh_ipaas_small.svg';
  iconDarkBg = 'assets/images/glasses_logo.svg';

  title = 'Red Hat iPaaS';
  url = 'https://www.twitter.com/jboss';
  loggedIn = false;

  user;

  constructor(configService: ConfigService, private oauthService: OAuthService, private userService: UserService) {
    // URL of the SPA to redirect the user to after login
    oauthService.redirectUri = window.location.origin + '/dashboard';

    // The SPA's id. The SPA is registerd with this id at the auth-server
    oauthService.clientId = configService.getSettings('oauth', 'clientId');

    // set the scope for the permissions the client should request
    // The first three are defined by OIDC. The 4th is a usecase-specific one
    oauthService.scope = (configService.getSettings('oauth', 'scopes') as string[]).join(' ');

    // Use setStorage to use sessionStorage or another implementation of the TS-type Storage
    // instead of localStorage
    oauthService.setStorage(sessionStorage);

    // Login-Url
    oauthService.loginUrl = configService.getSettings('oauth', 'authorize');
    this.oauthService.userinfoEndpoint = configService.getSettings('oauth', 'userInfo');

    this.oauthService.tryLogin({
      onTokenReceived: (_context) => {
        this.oauthService.loadUserProfile().then(() => {
          this.userService.setUser(this.oauthService.getIdentityClaims());
        });
      },
    });

    this.user = userService.user;
  }

  ngOnInit() {
    this.loggedIn = this.oauthService.hasValidAccessToken();
  }

  ngAfterViewInit() {
    $(document).ready(function () {
      // matchHeight the contents of each .card-pf and then the .card-pf itself
      $(".row-cards-pf > [class*='col'] > .card-pf .card-pf-title").matchHeight();
      $(".row-cards-pf > [class*='col'] > .card-pf > .card-pf-body").matchHeight();
      $(".row-cards-pf > [class*='col'] > .card-pf > .card-pf-footer").matchHeight();
      $(".row-cards-pf > [class*='col'] > .card-pf").matchHeight();

      // Initialize the vertical navigation
      $().setupVerticalNavigation(true);
    });
  }
}
