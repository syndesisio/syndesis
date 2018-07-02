import { Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';

import { UserService, ApiHttpService, User } from '@syndesis/ui/platform';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class UserProviderService extends UserService {
  private user$: Observable<User>;

  /**
   * UserService constructor
   * @param {HttpClient} httpClient
   */
  constructor(
    private httpClient: HttpClient,
    private apiHttpService: ApiHttpService,
    private ngZone: NgZone
  ) {
    super();
  }

  get user(): Observable<User> {
    if (!this.user$) {
      this.user$ = this.apiHttpService.setEndpointUrl('/users/~').get<User>();
    }
    return this.user$;
  }

  /**
   * Triggers the logout flow and effectively returns the user to the login page
   */
  logout(): void {
    this.ngZone.runOutsideAngular(() => {
      window.location.href = '/oauth/sign_out';
    });
  }
}
