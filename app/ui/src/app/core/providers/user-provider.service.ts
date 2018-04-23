import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { UserService, ApiHttpService, User } from '@syndesis/ui/platform';
import {
  HttpClient,
  HttpHeaders,
  HttpRequest,
  HttpEventType,
  HttpProgressEvent,
  HttpResponse,
  HttpUrlEncodingCodec
} from '@angular/common/http';

@Injectable()
export class UserProviderService extends UserService {
  private user$: Observable<User>;

  /**
   * UserService constructor
   * @param {HttpClient} httpClient
   */
  constructor(private httpClient: HttpClient, private apiHttpService: ApiHttpService) {
    super();
  }

  get user(): Observable<User> {
    if (!this.user$) {
      this.user$ = this.apiHttpService.setEndpointUrl('/users/~').get<User>();
    }
    return this.user$;
  }

  /**
   * Log the user out
   */
  logout(): Observable<any> {
    return this.httpClient.get('/oauth/sign_out');
  }
}
