import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { Action, Connection, ApiHttpService } from '@syndesis/ui/platform';
import { Exchange } from '@syndesis/ui/model';
import { Integration } from './integration.model';
import { integrationSupportEndpoints } from './integration-support.api';

@Injectable()
export class IntegrationSupportService {

  constructor(private apiHttpService: ApiHttpService) {}

  getFilterOptions(dataShape: any): Observable<any> {
    return this.apiHttpService.post(integrationSupportEndpoints.filterOptions, dataShape);
  }

  getHistory(id: string): Observable<any> {
    return this.apiHttpService.setEndpointUrl(integrationSupportEndpoints.history, { id }).get();
  }

  getDeployments(id: string): Observable<any> {
    return this.apiHttpService.setEndpointUrl(integrationSupportEndpoints.deployments, { id }).get();
  }

  getDeployment(id: string, version: string): Observable<any> {
    return this.apiHttpService.setEndpointUrl(integrationSupportEndpoints.deployment, { id, version }).get();
  }

  requestPom(integration: Integration): Observable<any> {
    return this.apiHttpService.post(integrationSupportEndpoints.pom, integration);
  }

  fetchMetadata(
    connection: Connection,
    action: Action,
    configuredProperties: any
  ): Observable<any> {
    return this.apiHttpService.setEndpointUrl(integrationSupportEndpoints.metadata, {
      connectionId: connection.id,
      actionId: action.id
    }).post(configuredProperties);
  }

  requestJavaInspection(
    connectorId: String,
    type: String
  ): Observable<Response> {
    return this.apiHttpService.setEndpointUrl(integrationSupportEndpoints.javaInspection, { connectorId, type }).get();
  }

  exportIntegration(...ids: string[]): Observable<Blob> {
    const url = integrationSupportEndpoints.export + '?' + ids.map(id => 'id=' + id).join('&');
    return this.apiHttpService.get<Blob>(url, { responseType: 'blob' });
  }

  importIntegrationURL(): string {
    return this.apiHttpService.getEndpointUrl(integrationSupportEndpoints.import);
  }

  requestIntegrationLogs(id: string): Observable<Exchange[]> {
    return this.apiHttpService.setEndpointUrl(integrationSupportEndpoints.logs, {
      integrationId: id,
    }).get().map(res => {
      const transactions = res as Exchange[];
      return transactions;
    });
  }

}
