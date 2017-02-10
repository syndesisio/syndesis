import { Injectable } from '@angular/core';
import { CanActivate, CanActivateChild } from '@angular/router';
import { OAuthService } from 'angular-oauth2-oidc';

import { UserService } from '../common/user.service';

@Injectable()
export class AuthGuard implements CanActivate, CanActivateChild {

  constructor(private oauthService: OAuthService, private userService: UserService) { }

  canActivate(): boolean {
    return this.oauthService.hasValidAccessToken();
  }

  canActivateChild(): boolean {
    return this.oauthService.hasValidAccessToken();
  }

}
