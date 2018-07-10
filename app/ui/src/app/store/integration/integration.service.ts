import { Injectable } from '@angular/core';
import { ApiHttpService,
  Integration,
  Integrations,
  IntegrationSupportService,
  PENDING,
  IntegrationStatusDetail } from '@syndesis/ui/platform';
import { forkJoin, Observable, of } from 'rxjs';
import { map, mergeMap, switchMap, catchError } from 'rxjs/operators';
import { RESTService } from '../entity';
import { log, getCategory } from '../../logging';

@Injectable()
export class IntegrationService extends RESTService<Integration, Integrations> {
  constructor(apiHttpService: ApiHttpService,
              protected integrationSupportService: IntegrationSupportService) {
    super(apiHttpService, 'integrations', 'integration');
  }

  get(id: string): Observable<Integration> {
    return forkJoin<Integration, IntegrationStatusDetail>([
      super.get(id),
      this.integrationSupportService.fetchDetailedStatus(id).pipe(
        catchError( err => {
          // Fall back to showing the coarse status
          log.warn('error fetching detailed status: ', err);
          return undefined;
        })
      )
    ]).pipe(
      map(results => {
        const integration = results[0];
        const detailedStatus = results[1];
        integration.statusDetail = detailedStatus;
        return integration;
      })
    );
  }

  list(): Observable<Integrations> {
    return forkJoin<Integrations, IntegrationStatusDetail[]>([
      super.list(),
      this.integrationSupportService.fetchDetailedStatuses().pipe(
        catchError( err => {
          // Fall back to showing the coarse status
          log.warn('error fetching detailed statuses: ', err);
          return [];
        })
      )
    ]).pipe(
      map( results => {
        const integrations = <Integrations> results[0];
        const statuses = <IntegrationStatusDetail[]> results [1];
        statuses.forEach( status => {
          const integration = integrations.find(i => i.id === status.integrationId);
          if (integration) {
            integration.statusDetail = status;
          }
        });
        return integrations;
      })
    );
  }

}
