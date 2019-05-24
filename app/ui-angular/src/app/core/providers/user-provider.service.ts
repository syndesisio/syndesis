import { Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';

import { UserService, ApiHttpService, User } from '@syndesis/ui/platform';
import { HttpClient, HttpXsrfTokenExtractor } from '@angular/common/http';

import { environment } from 'environments/environment';

@Injectable()
export class UserProviderService extends UserService {
  private user$: Observable<User>;

  constructor(
    private httpClient: HttpClient,
    private apiHttpService: ApiHttpService,
    private ngZone: NgZone,
    private tokenExtractor: HttpXsrfTokenExtractor
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
   * Triggers the logout flow and renders the HTML response
   */
  logout(): void {
    this.user$ = null;
    this.ngZone.runOutsideAngular(() => {
      const headers = {};
      const token = this.tokenExtractor.getToken() || environment.xsrf.defaultTokenValue;

      headers[environment.xsrf.headerName] = token;
      this.httpClient.get('/logout', {
        headers: headers,
        responseType: 'arraybuffer'
      }).subscribe(buffy => {
        const html = String.fromCharCode.apply(null, new Uint8Array(buffy));
        window.history.pushState(null, null, '/logout');
        window.document.open();
        window.document.write(html);
        window.document.close();
      });
    });
  }
}
