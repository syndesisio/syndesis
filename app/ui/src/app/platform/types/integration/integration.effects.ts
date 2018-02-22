import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';

import { IntegrationSupportService } from './integration-support.service';
import { IntegrationService } from './integration.service';
import * as IntegrationActions from './integration.actions';

@Injectable()
export class IntegrationEffects {

  @Effect()
  fetchIntegrations$: Observable<Action> = this.actions$
    .ofType(IntegrationActions.FETCH_INTEGRATIONS)
    .mergeMap(() =>
      this.integrationService
        .fetch()
        .map(response => ({ type: IntegrationActions.FETCH_INTEGRATIONS_COMPLETE, payload: response }))
        .catch(error => Observable.of({
          type: IntegrationActions.FETCH_INTEGRATIONS_FAIL,
          payload: error
        }))
    );

  @Effect()
  fetchIntegrationMetrics$: Observable<Action> = this.actions$
    .ofType<IntegrationActions.FetchMetrics>(
      IntegrationActions.FETCH_METRICS,
      IntegrationActions.FETCH_INTEGRATIONS
    )
    .mergeMap(action =>
      this.integrationService
        .fetchMetrics(action.id)
        .map(response => ({ type: IntegrationActions.FETCH_METRICS_COMPLETE, payload: response || {} }))
        .catch(error => Observable.of({
          type: IntegrationActions.FETCH_METRICS_FAIL,
          payload: error
        }))
    );

  @Effect()
  appendIntegrationOverviews$: Observable<Action> = this.actions$
    .ofType<IntegrationActions.FetchMetricsComplete>(
      IntegrationActions.FETCH_INTEGRATIONS_COMPLETE
    )
    .mergeMap(action =>
      this.integrationSupportService
        .watchOverviews()
        .map(response => ({ type: IntegrationActions.REFRESH_OVERVIEWS, payload: response }))
        .catch(error => Observable.of({
          type: IntegrationActions.REFRESH_OVERVIEWS_FAIL,
          payload: error
        }))
    );

  constructor(
    private actions$: Actions,
    private integrationService: IntegrationService,
    private integrationSupportService: IntegrationSupportService,
  ) {}
}
