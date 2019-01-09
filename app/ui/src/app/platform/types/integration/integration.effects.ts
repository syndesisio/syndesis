import { of as observableOf, Observable } from 'rxjs';

import { map, mergeMap, catchError } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Actions, Effect, ofType } from '@ngrx/effects';

import { IntegrationService } from '@syndesis/ui/platform/types/integration/integration.service';
import * as IntegrationActions from '@syndesis/ui/platform/types/integration/integration.actions';

@Injectable()
export class IntegrationEffects {
  @Effect()
  fetchIntegrations$: Observable<Action> = this.actions$
    .pipe(
      ofType(IntegrationActions.FETCH_INTEGRATIONS),
      mergeMap(() =>
        this.integrationService.fetch().pipe(
          map(response => ({
            type: IntegrationActions.FETCH_INTEGRATIONS_COMPLETE,
            payload: response
          })),
          catchError(error =>
            observableOf({
              type: IntegrationActions.FETCH_INTEGRATIONS_FAIL,
              payload: error
            })
          )
        )
      )
    );

  @Effect()
  fetchIntegrationMetrics$: Observable<Action> = this.actions$
    .pipe(
      ofType<IntegrationActions.FetchMetrics>(
        IntegrationActions.FETCH_METRICS,
        IntegrationActions.FETCH_INTEGRATIONS
      ),
      mergeMap(action =>
        this.integrationService.fetchMetrics(action.id).pipe(
          map(response => ({
            type: IntegrationActions.FETCH_METRICS_COMPLETE,
            payload: { id: action.id, ...response }
          })),
          catchError(error =>
            observableOf({
              type: IntegrationActions.FETCH_METRICS_FAIL,
              payload: error
            })
          )
        )
      )
    );

  constructor(
    private actions$: Actions,
    private integrationService: IntegrationService
  ) {}
}
