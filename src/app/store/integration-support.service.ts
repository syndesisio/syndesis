import { Injectable } from '@angular/core';
import { Http, Headers, RequestOptions } from '@angular/http';
import { OAuthService } from 'angular-oauth2-oidc-hybrid';
import { Observable } from 'rxjs/Observable';

import { Restangular } from 'ngx-restangular';

import { Integration } from '../model';

@Injectable()
export class IntegrationSupportService {
  service: Restangular = undefined;
  filterService: Restangular = undefined;

  constructor(public restangular: Restangular,
              public http: Http,
              public oauth: OAuthService,) {
    this.service = restangular.service('integration-support');
    this.filterService = restangular.service('/');
  }

  getFilterOptions(connectionId, actionId): Observable<any> {
    // Should be /{connectorId}/actions/{actionId}/filters/options
    const url = this.filterService.one(connectionId).one('actions', actionId).one('filters').one('options').getRestangularUrl();
    const headers = new Headers({
      Authorization: 'Bearer ' + this.oauth.getAccessToken(),
    });
    const options = new RequestOptions({ headers: headers });
    return this.http.get(url, options);
  }

  requestPom(integration: Integration) {
    const url = this.service.one('generate').one('pom.xml').getRestangularUrl();
    const headers = new Headers({
      Authorization: 'Bearer ' + this.oauth.getAccessToken(),
    });
    const options = new RequestOptions({ headers: headers });
    return this.http.post(url, integration, options);
  }
}
