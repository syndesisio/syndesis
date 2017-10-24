import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Restangular } from 'ngx-restangular';
import { Observable } from 'rxjs/Observable';

import { ConfigService } from '../config.service';
import { Action, Connection, Integration } from '../model';

@Injectable()
export class IntegrationSupportService {
  mapperBaseUrl: string = undefined;
  service: Restangular = undefined;
  filterService: Restangular = undefined;
  metadataService: Restangular = undefined;
  configService: ConfigService = undefined;

  constructor(public restangular: Restangular,
              public http: Http,
              public config: ConfigService) {
    this.service = restangular.service('integration-support');
    this.filterService = restangular.service('integrations');
    this.metadataService = restangular.service('connections');
    this.configService = config;
    this.mapperBaseUrl = config.getSettings().apiBase + config.getSettings().mapperEndpoint;
  }

  getFilterOptions(dataShape: any): Observable<any> {
    const url = this.filterService.one('filters').one('options').getRestangularUrl();
    return this.http.post(url, dataShape);
  }

  requestPom(integration: Integration) {
    const url = this.service.one('generate').one('pom.xml').getRestangularUrl();
    return this.http.post(url, integration);
  }

  fetchMetadata(connection: Connection, action: Action, configuredProperties: any) {
    const connectionId = connection.id;
    const actionId = action.id;
    const url = this.metadataService.one(connectionId).one('actions').one(actionId).getRestangularUrl();
    return this.http.post(url, configuredProperties);
  }

  requestJavaInspection(connectorId: String, type: String) {
    const url = this.mapperBaseUrl + '/java-inspections/' + connectorId + '/' + type + '.json';
    return this.http.get(url);
  }
}
