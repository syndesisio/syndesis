import { Injectable } from '@angular/core';
import { Http, Headers, RequestOptions } from '@angular/http';
import { OAuthService } from 'angular-oauth2-oidc-hybrid';

import { Restangular } from 'ngx-restangular';

import { Integration } from '../model';

@Injectable()
export class IntegrationSupportService {

  service: Restangular = undefined;

  constructor(
    public restangular: Restangular,
    public http: Http,
    public oauth: OAuthService,
  ) {
    this.service = restangular.service('integration-support');
  }

  requestPom(integration: Integration) {
    const url = this.service.one('generate').one('pom.xml').getRestangularUrl();
    const headers = new Headers({ Authorization: 'Bearer ' + this.oauth.getAccessToken() });
    const options = new RequestOptions({ headers: headers });
    return this.http.post(url, integration, options);
  }

}
