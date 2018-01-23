import { Injectable, Input } from '@angular/core';
import { Observable } from 'rxjs/Rx';

import { UserService, ApiHttpService } from '@syndesis/ui/platform';
import { User } from '@syndesis/ui/model';

@Injectable()
export class UserProviderService extends UserService {
  private user$: Observable<User>;

  /**
   * UserService constructor
   * @param {HttpClient} httpClient
   */
  constructor(private apiHttpService: ApiHttpService) {
    super();
  }

  get user(): Observable<User> {
    if (!this.user$) {
      this.user$ = this.apiHttpService.setEndpointUrl('/users').get<User>();
    }

    return this.user$;
  }

  /**
   * Set state of Guided Tour
   */
  setTourState(val): void {
    return localStorage.setItem('guidedTourState', JSON.stringify(val));
  }

  /**
   * Get state of Guided Tour
   */
  getTourState() {
    return JSON.parse(localStorage.getItem('guidedTourState'));
  }

  /**
   * Log the user out
   */
  logout(): Observable<any> {
    return this.apiHttpService.setEndpointUrl('/oauth/sign_out').get();
  }
}
