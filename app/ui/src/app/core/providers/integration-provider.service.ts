import { map } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  BaseEntity,
  ApiHttpService,
  IntegrationService,
  Integration,
  Integrations,
  integrationEndpoints,
  IntegrationMetrics
} from '@syndesis/ui/platform';

@Injectable()
export class IntegrationProviderService extends IntegrationService {
  constructor(private apiHttpService: ApiHttpService) {
    super();
  }

  fetch(): Observable<Integrations> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.integrations)
      .get<{ items: Integrations }>()
      .pipe(map(response => response.items));
  }

  create(integration: Integration): Observable<any> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.integrations)
      .post(integration);
  }

  update(integration: Integration): Observable<any> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.integration, { id: integration.id })
      .put(integration);
  }

  delete(integration: BaseEntity): Observable<any> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.integration, { id: integration.id })
      .delete();
  }

  fetchMetrics(id?: string): Observable<IntegrationMetrics> {
    const endpointKey = id
      ? integrationEndpoints.integrationMetricsById
      : integrationEndpoints.integrationMetrics;
    return this.apiHttpService
      .setEndpointUrl(endpointKey, { id })
      .get<IntegrationMetrics>();
  }
}
