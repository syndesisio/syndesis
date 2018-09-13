import { Injectable } from '@angular/core';
import {
  ApiHttpService,
  DetailedState,
  Integration,
  Integrations,
  IntegrationStatusDetail,
  IntegrationSupportService,
  IntegrationType
} from '@syndesis/ui/platform';
import { forkJoin, Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { RESTService } from '@syndesis/ui/store/entity';
import { log } from '@syndesis/ui/logging';
import { ConfigService } from '@syndesis/ui/config.service';

function transform(integration: Integration): Integration {
  if (integration.flows.length > 1) {
    integration.type = IntegrationType.ApiProvider;
  } else {
    integration.type = IntegrationType.SingleFlow;
  }
  return integration;
}

@Injectable()
export class IntegrationService extends RESTService<Integration, Integrations> {
  constructor(apiHttpService: ApiHttpService,
              configService: ConfigService,
              protected integrationSupportService: IntegrationSupportService) {
    super(apiHttpService, 'integrations', 'integration', configService, transform);
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
        // for styling
        //return this.forceDetailedState(integration);
        // for reals
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
        // force some state for styling
        /*
        return integrations.map((i, index) => {
          if (index === 0) {
            return this.forceDetailedState(i);
          }
          return i;
        });
        */

        // the real code
        return integrations;
      })
    );
  }

  // design time function
  private forceDetailedState(integration: Integration) {
    integration.currentState = 'Pending';
    integration.statusDetail = {
      detailedState: {
        value: 'ASSEMBLING',
        currentStep: 2,
        totalSteps: 4
      } as DetailedState,
      logsUrl: 'https://www.google.com',
    } as IntegrationStatusDetail;
    return integration;
  }

}
