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
    return super.get(id).pipe(
      switchMap(integration => this.checkIfPending(integration))
    );
  }

  list(): Observable<Integrations> {
    return forkJoin<Integrations, IntegrationStatusDetail[]>([
      super.list(),
      this.integrationSupportService.fetchDetailedStatuses().pipe(
        catchError( error => {
          // can always fall back to showing the coarse status
          log.warn('error fetching detailed status: ', error);
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

  private checkIfPending(integration): Observable<Integration> {
    if (integration.currentState === PENDING) {
      return this.fetchDetailedStatus(integration);
    } else {
      return of(integration);
    }
  }

  private fetchDetailedStatus(integration): Observable<Integration> {
    return this.integrationSupportService.fetchDetailedStatus(integration.id).pipe(
      map(detailedStatus => {
        integration.statusDetail = detailedStatus;
        return integration;
      })
    );
  }

}
