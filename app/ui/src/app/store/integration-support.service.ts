import { Injector, Injectable } from '@angular/core';
import { Http, Response, ResponseContentType, URLSearchParams } from '@angular/http';
import { Restangular } from 'ngx-restangular';
import { Observable } from 'rxjs/Observable';
import { ErrorObservable } from 'rxjs/observable/ErrorObservable';

import { RESTANGULAR_MAPPER } from '../app.module';
import { ConfigService } from '../config.service';
import { Action, Connection, Integration } from '../model';
import { log, getCategory } from '../logging';

@Injectable()
export class IntegrationSupportService {
  service: Restangular = undefined;
  filterService: Restangular = undefined;
  metadataService: Restangular = undefined;
  mapperService: Restangular = undefined;
  configService: ConfigService = undefined;

  constructor(
    restangular: Restangular,
    injector: Injector,
    private http: Http,
    private config: ConfigService
  ) {
    this.service = restangular.service('integration-support');
    this.filterService = restangular.service('integrations');
    this.metadataService = restangular.service('connections');
    const restangularMapper = injector.get(RESTANGULAR_MAPPER);
    this.mapperService = restangularMapper.service('java-inspections');
    this.configService = config;
  }

  getFilterOptions(dataShape: any): Observable<any> {
    const url = this.filterService
      .one('filters')
      .one('options')
      .getRestangularUrl();
    return this.http.post(url, dataShape);
  }

  requestPom(integration: Integration) {
    const url = this.service
      .one('generate')
      .one('pom.xml')
      .getRestangularUrl();
    return this.http.post(url, integration);
  }

  fetchMetadata(
    connection: Connection,
    action: Action,
    configuredProperties: any
  ) {
    const connectionId = connection.id;
    const actionId = action.id;
    const url = this.metadataService
      .one(connectionId)
      .one('actions')
      .one(actionId)
      .getRestangularUrl();
    return this.http.post(url, configuredProperties);
  }

  requestJavaInspection(
    connectorId: String,
    type: String
  ): Observable<Response> {
    const url = this.mapperService
      .one(connectorId)
      .one(type + '.json')
      .getRestangularUrl();
    return this.http.get(url);
  }

  exportIntegration(...ids: string[]): Observable<Response> {
    const params = new URLSearchParams();
    for ( const id of ids ) {
      params.append('id', id);
    }

    const url = this.service.one('export.zip').getRestangularUrl();
    return this.http.get(url, {
      responseType: ResponseContentType.Blob,
      params: params
    });
  }

  importIntegrationURL(): string {
    return this.service.one('import').getRestangularUrl();
  }
}
