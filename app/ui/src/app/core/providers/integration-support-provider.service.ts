import { merge as observableMerge, Observable } from 'rxjs';

import { mergeMap, map, filter } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import {
  Action,
  Connection,
  Activity,
  Integration,
  IntegrationDeployment,
  IntegrationDeployments,
  IntegrationOverview,
  IntegrationOverviews,
  IntegrationStatus,
  IntegrationSupportService,
  ApiHttpService,
  integrationEndpoints,
  UNPUBLISHED,
  PUBLISHED,
  IntegrationStatusDetail
} from '@syndesis/ui/platform';
import { EventsService } from '@syndesis/ui/store';

@Injectable()
export class IntegrationSupportProviderService extends IntegrationSupportService {
  constructor(
    private apiHttpService: ApiHttpService,
    private eventsService: EventsService
  ) {
    super();
  }

  getFilterOptions(dataShape: any): Observable<any> {
    return this.apiHttpService.post(
      integrationEndpoints.filterOptions,
      dataShape
    );
  }

  deploy(integration: Integration | IntegrationDeployment): Observable<any> {
    let url, state, method;
    if ('integrationVersion' in integration) {
      // it's an IntegrationDeployment
      url = integrationEndpoints.updateState;
      state = { targetState: PUBLISHED };
      method = 'post';
    } else {
      // it's an Integration
      url = integrationEndpoints.publish;
      state = {};
      method = 'put';
    }
    return this.apiHttpService
      .setEndpointUrl(url, { id: integration.id, version: integration.version })
      [method](state);
  }

  undeploy(integration: Integration): Observable<any> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.updateState, {
        id: integration.id,
        version: integration.deploymentVersion
      })
      .post({
        targetState: UNPUBLISHED
      });
  }

  updateState(
    id: string,
    version: string | number,
    status: IntegrationStatus
  ): Observable<any> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.updateState, { id, version })
      .post({
        targetState: status
      });
  }

  getDeployment(
    id: string,
    version: string
  ): Observable<IntegrationDeployment> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.deployment, { id, version })
      .get();
  }

  getDeployments(id: string): Observable<IntegrationDeployments> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.deployments, { id })
      .get()
      .pipe(
        map(response => (response['items'] as IntegrationDeployments) || [])
      );
  }

  watchDeployments(id: string): Observable<any> {
    return observableMerge(
      this.getDeployments(id),
      this.eventsService.changeEvents.pipe(
        filter(event => event.kind === 'integration-deployment'),
        // TODO it would obviously be better to just fetch one, not all of 'em
        mergeMap(event => this.getDeployments(id))
      )
    );
  }

  requestPom(integration: Integration): Observable<any> {
    return this.apiHttpService.post(integrationEndpoints.pom, integration);
  }

  fetchMetadata(
    connection: Connection,
    action: Action,
    configuredProperties: any
  ): Observable<any> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.metadata, {
        connectionId: connection.id,
        actionId: action.id
      })
      .post(configuredProperties);
  }

  requestJavaInspection(
    connectorId: String,
    type: String
  ): Observable<Response> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.javaInspection, {
        connectorId,
        type
      })
      .get();
  }

  exportIntegration(...ids: string[]): Observable<Blob> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.export, { id: ids })
      .get({ responseType: 'blob' });
  }

  importIntegrationURL(): string {
    return this.apiHttpService.getEndpointUrl(integrationEndpoints.import);
  }

  requestIntegrationActivityFeatureEnabled(): Observable<boolean> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.activityFeature)
      .get<{ enabled?: string }>()
      .pipe(map(response => !!response.enabled));
  }

  requestIntegrationActivity(integrationId: string): Observable<Activity[]> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.activity, { integrationId })
      .get<Activity[]>();
  }

  downloadSupportData(data: any[]): Observable<Blob> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.supportData)
      .post<Blob>(data, { responseType: 'blob' });
  }

  fetchDetailedStatus(id: string): Observable<IntegrationStatusDetail> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.integrationStatusDetail, { id })
      .get<IntegrationStatusDetail>();
  }

  fetchDetailedStatuses(): Observable<IntegrationStatusDetail[]> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.integrationStatusDetails)
      .get<IntegrationStatusDetail[]>();
  }

  private getOverview(id: string): Observable<any> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.overview, { id })
      .get();
  }

  private watchOverview(id: string): Observable<IntegrationOverview> {
    return observableMerge(
      this.getOverview(id),
      this.eventsService.changeEvents.pipe(
        filter(event => {
          switch (event.kind) {
            case 'integration':
              return event.id === id;
            case 'integration-deployment':
              return event.id.startsWith(id);
            default:
              return false;
          }
        }),
        mergeMap(event => this.getOverview(id))
      )
    );
  }

  private getOverviews(): Observable<IntegrationOverviews> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.overviews)
      .get()
      .pipe(map((value: any) => value.items || []));
  }

  private watchOverviews(): Observable<IntegrationOverviews> {
    return observableMerge(
      this.getOverviews(),
      this.eventsService.changeEvents.pipe(
        filter(
          event =>
            event.kind === 'integration' ||
            event.kind === 'integration-deployment'
        ),
        mergeMap(event => this.getOverviews())
      )
    );
  }
}
