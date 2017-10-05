import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';

import { User } from '../model';
import { OAuthService } from 'angular-oauth2-oidc-hybrid';

@Injectable()
export class UserService {
  private _user = new BehaviorSubject(<User>{});

  constructor(private oauthService: OAuthService) {}

  get user() {
    return this._user.asObservable();
  }

  /**
   * Checks to see if user has a token or not
   */
  checkToken() {
    return this.oauthService.hasValidAccessToken();
  }

  setUser(u: User) {
    this._user.next(u);
  }
}
