
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { Action, Connection, Activity, Integration, IntegrationDeployment, IntegrationDeployments } from '@syndesis/ui/platform';

@Injectable()
export abstract class IntegrationSupportService {

  abstract getFilterOptions(dataShape: any): Observable<any>;
  abstract getHistory(id: string): Observable<any>;
  abstract getDeployments(id: string): Observable<IntegrationDeployments>;
  abstract watchDeployments(id: string): Observable<any>;
  abstract getDeployment(id: String, version: string): Observable<IntegrationDeployment>;
  abstract requestPom(integration: Integration): Observable<any>;
  abstract fetchMetadata(connection: Connection, action: Action, configuredProperties: any): Observable<any>;
  abstract requestJavaInspection(connectorId: string, type: string);
  abstract exportIntegration(...ids: string[]): Observable<Blob>;
  abstract importIntegrationURL(): string;
  abstract requestIntegrationActivityFeatureEnabled(): Observable<boolean>;
  abstract requestIntegrationActivity(id: string): Observable<Activity[]>;
  abstract downloadSupportData(data: any[]): Observable<Blob>;
}
