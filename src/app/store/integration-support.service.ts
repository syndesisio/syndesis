import { Injectable } from '@angular/core';
import { Http, Headers, RequestOptions } from '@angular/http';
import { OAuthService } from 'angular-oauth2-oidc-hybrid';
import { Observable } from 'rxjs/Observable';

import { Restangular } from 'ngx-restangular';

import { Integration, Connection, Action } from '../model';

@Injectable()
export class IntegrationSupportService {
  service: Restangular = undefined;
  filterService: Restangular = undefined;
  metadataService: Restangular = undefined;

  constructor(public restangular: Restangular,
              public http: Http,
              public oauth: OAuthService) {
    this.service = restangular.service('integration-support');
    this.filterService = restangular.service('integrations');
    this.metadataService = restangular.service('connections');
  }

  private getRequestOptions() {
    const headers = new Headers({
      Authorization: 'Bearer ' + this.oauth.getAccessToken(),
    });
    const options = new RequestOptions({ headers: headers });
    return options;
  }

  getFilterOptions(dataShape: any): Observable<any> {
    const url = this.filterService.one('filters').one('options').getRestangularUrl();
    return this.http.post(url, dataShape, this.getRequestOptions());
  }

  requestPom(integration: Integration) {
    const url = this.service.one('generate').one('pom.xml').getRestangularUrl();
    return this.http.post(url, integration, this.getRequestOptions());
  }

  fetchMetadata(connection: Connection, action: Action, configuredProperties: any) {
    const connectionId = connection.id;
    const actionId = action.id;
    const url = this.metadataService.one(connectionId).one('actions').one(actionId).getRestangularUrl();
    return this.http.post(url, configuredProperties, this.getRequestOptions());
  }
}
