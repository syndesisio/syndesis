import { Injectable } from '@angular/core';
import { Http, Headers, RequestOptions } from '@angular/http';
import { OAuthService } from 'angular-oauth2-oidc-hybrid';

import { Restangular } from 'ngx-restangular';

import { Integration } from '../model';

@Injectable()
export class TestSupportService {

  service: Restangular = undefined;

  constructor(
    public restangular: Restangular,
    public http: Http,
    public oauth: OAuthService,
  ) {
    this.service = restangular.service('test-support');
  }

  resetDB() {
    const url = this.service.one('reset-db').getRestangularUrl();
    const headers = new Headers({ Authorization: 'Bearer ' + this.oauth.getAccessToken() });
    const options = new RequestOptions({ headers: headers });
    return this.http.get(url, options);
  }

  snapshotDB() {
    const url = this.service.one('snapshot-db').getRestangularUrl();
    const headers = new Headers({ Authorization: 'Bearer ' + this.oauth.getAccessToken() });
    const options = new RequestOptions({ headers: headers });
    return this.http.get(url, options);
  }

  restoreDB(data: any) {
    const url = this.service.one('restore-db').getRestangularUrl();
    const headers = new Headers({ Authorization: 'Bearer ' + this.oauth.getAccessToken() });
    const options = new RequestOptions({ headers: headers });
    return this.http.post(url, data, options);
  }

}
