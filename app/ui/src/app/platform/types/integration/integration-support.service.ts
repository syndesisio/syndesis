import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import {
  Action,
  Activity,
  Connection,
  Integration,
  IntegrationDeployment,
  IntegrationDeployments,
  IntegrationOverview,
  IntegrationOverviews,
  IntegrationStatus,
  ApiHttpService,
} from '@syndesis/ui/platform';

@Injectable()
export abstract class IntegrationSupportService {

  /**
   * Fetch the options object for the rule filter
   * @param dataShape
   */
  abstract getFilterOptions(dataShape: any): Observable<any>;

  /**
   * Fetch the overview for an integration
   * @param id
   */
  abstract getOverview(id: string): Observable<IntegrationOverview>;

  /**
   * Fetch the overview for an integration and watch for changes
   * @param id
   */
  abstract watchOverview(id: string): Observable<IntegrationOverview>;

  /**
   * Fetch all overview objects for all integrations
   */
  abstract getOverviews(): Observable<IntegrationOverviews>;
  /**
   * Fetch all overview objects and watch for changes
   */
  abstract watchOverviews(): Observable<IntegrationOverviews>;

  abstract deploy(integration: Integration | IntegrationDeployment): Observable<any>;

  abstract undeploy(integration: Integration): Observable<any>;

  /**
   * Change the state of a running integration
   */
  abstract updateState(id: string, version: string | number, status: IntegrationStatus): Observable<any>;

  /**
   * Fetch a single deployment version for an integration
   * @param id
   * @param version
   */
  abstract getDeployment(id: String, version: string): Observable<IntegrationDeployment>;

  /**
   * Fetch all the deployments for an integration
   * @param id
   */
  abstract getDeployments(id: string): Observable<IntegrationDeployments>;

  /**
   * Fetch all the deployments for an integration and watch for changes
   * @param id
   */
  abstract watchDeployments(id: string): Observable<any>;

  /**
   * Request the effective POM for an integration
   * @param integration
   */
  abstract requestPom(integration: Integration): Observable<any>;

  /**
   * Fetch the metadata associated with the supplied action
   * @param connection
   * @param action
   * @param configuredProperties
   */
  abstract fetchMetadata(connection: Connection, action: Action, configuredProperties: any): Observable<any>;

  /**
   * Create a java inspection for the given data type
   * @param connectorId
   * @param type
   */
  abstract requestJavaInspection(connectorId: string, type: string);

  /**
   * Export the supplied integration(s) to a zip file
   * @param ids
   */
  abstract exportIntegration(...ids: string[]): Observable<Blob>;

  /**
   * Return the URL used to post an imported integration
   */
  abstract importIntegrationURL(): string;
  abstract requestIntegrationActivityFeatureEnabled(): Observable<boolean>;
  abstract requestIntegrationActivity(id: string): Observable<Activity[]>;

  abstract downloadSupportData(data: any[]): Observable<Blob>;
}
