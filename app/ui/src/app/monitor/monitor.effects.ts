import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';

import { MonitorService } from './monitor.service';
import * as MonitorActions from './monitor.actions';

@Injectable()
export class MonitorEffects {
  @Effect()
  fetchMonitorMetrics$: Observable<Action> = this.actions$
    .ofType(MonitorActions.FETCH_METRICS)
    .mergeMap(() =>
      this.monitorService
        .getIntegrationMetrics()
        .map(response => ({ type: MonitorActions.FETCH_METRICS_COMPLETE, payload: response }))
        .catch(error => Observable.of({
          type: MonitorActions.FETCH_METRICS_FAIL,
          payload: error
        }))
    );

  @Effect()
  fetchIntegrationLogs$: Observable<Action> = this.actions$
    .ofType<MonitorActions.LogsFetch>(MonitorActions.FETCH_LOGS)
    .mergeMap(action =>
      this.monitorService
        .getLogs(action.payload)
        .map(response => ({ type: MonitorActions.FETCH_LOGS_COMPLETE, payload: response }))
        .catch(error => Observable.of({
          type: MonitorActions.FETCH_LOGS_FAIL,
          payload: error
        }))
    );

  @Effect()
  fetchIntegrationLogDelta$: Observable<Action> = this.actions$
    .ofType<MonitorActions.LogDeltasFetch>(MonitorActions.FETCH_LOG_DELTAS)
    .mergeMap(action =>
      this.monitorService
        .getLogDeltas(action.payload)
        .map(response => ({ type: MonitorActions.FETCH_LOGS_COMPLETE, payload: response }))
        .catch(error => Observable.of({
          type: MonitorActions.FETCH_LOGS_FAIL,
          payload: error
        }))
    );

  constructor(
    private actions$: Actions,
    private monitorService: MonitorService
  ) { }
}
