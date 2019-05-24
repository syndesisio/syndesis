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
  IntegrationStatus,
  IntegrationSupportService,
  ApiHttpService,
  integrationEndpoints,
  UNPUBLISHED,
  PUBLISHED,
  IntegrationStatusDetail,
  ContinuousDeliveryEnvironment,
  Step
} from '@syndesis/ui/platform';
import { EventsService } from '@syndesis/ui/store';
import { HttpHeaders } from '@angular/common/http';

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
        version: integration.version,
      })
      .post({
        targetState: UNPUBLISHED,
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
        targetState: status,
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
        actionId: action.id,
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
        type,
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

  fetchIntegrationTags(
    integrationId: string
  ): Observable<Map<String, ContinuousDeliveryEnvironment>> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.tags, { integrationId })
      .get<any>();
  }

  tagIntegration(
    integrationId: string,
    environments: string[]
  ): Observable<Map<String, ContinuousDeliveryEnvironment>> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.tags, { integrationId })
      .put<any>(environments);
  }

  removeEnvironment(env: string): Observable<void> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.deleteEnvironment, {
        env: encodeURIComponent(env),
      })
      .delete();
  }

  renameEnvironment(oldEnv: string, newEnv: string): Observable<void> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.renameEnvironment, {
        env: encodeURIComponent(oldEnv),
      })
      .put(newEnv, {
        headers: new HttpHeaders({
          'Content-type': 'application/json',
        }),
      });
  }

  getEnvironments(): Observable<string[]> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.environments)
      .get();
  }

  getStepDescriptors(
    steps: Step[]
  ): Observable<Step[]> {
    return this.apiHttpService
      .setEndpointUrl(integrationEndpoints.getStepDescriptors)
      .post(steps);
  }
}
