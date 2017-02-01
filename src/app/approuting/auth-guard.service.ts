import { Injectable } from '@angular/core';
import { CanActivate, CanActivateChild } from '@angular/router';
import { OAuthService } from 'angular-oauth2-oidc';

import { UserService } from '../common/user.service';

@Injectable()
export class AuthGuard implements CanActivate, CanActivateChild {

  constructor(private oauthService: OAuthService, private userService: UserService) { }

  canActivate(): boolean {
    const loggedIn = this.oauthService.hasValidAccessToken();
    if (!loggedIn) {
      this.oauthService.initImplicitFlow();
    } else {
      this.oauthService.loadUserProfile().then(() => {
        this.userService.setUser(this.oauthService.getIdentityClaims());
      });
    }

    return loggedIn;
  }

  canActivateChild(): boolean {
    return this.oauthService.hasValidAccessToken();
  }

}
