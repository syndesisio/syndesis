import { Action } from '@ngrx/store';

import { ActionReducerError } from '@syndesis/ui/platform';
import { Integration } from '@syndesis/ui/integration';

export const FETCH_METRICS              = '[Monitor] Fetch metrics request';
export const FETCH_METRICS_COMPLETE     = '[Monitor] Fetch metrics complete';
export const FETCH_METRICS_FAIL         = '[Monitor] Fetch metrics failed';
export const FETCH_LOGS                 = '[Monitor] Fetch logs request';
export const FETCH_LOGS_COMPLETE        = '[Monitor] Fetch logs complete';
export const FETCH_LOGS_FAIL            = '[Monitor] Fetch logs failed';
export const FETCH_LOG_DELTAS           = '[Monitor] Fetch latest log deltas ';
export const FETCH_LOG_DELTAS_COMPLETE  = '[Monitor] New log deltas appendend to store';

export class MetricsFetch implements Action {
  readonly type = FETCH_METRICS;
}

export class MetricsFetchComplete implements Action {
  readonly type = FETCH_METRICS_COMPLETE;

  constructor(public payload: any) { }
}

export class MetricsFetchFail implements Action {
  readonly type = FETCH_METRICS_FAIL;

  constructor(public payload: ActionReducerError) { }
}

export class LogsFetch implements Action {
  readonly type = FETCH_LOGS;

  constructor(public payload: Integration) { }
}

export class LogsFetchComplete implements Action {
  readonly type = FETCH_LOGS_COMPLETE;

  constructor(public payload: any) { }
}

export class LogsFetchFail implements Action {
  readonly type = FETCH_LOGS_FAIL;

  constructor(public payload: ActionReducerError) { }
}

export class LogDeltasFetch implements Action {
  readonly type = FETCH_LOG_DELTAS;

  constructor(public payload: { integration: Integration; timestamp: number; }) { }
}

export class LogDeltasFetchComplete implements Action {
  readonly type = FETCH_LOG_DELTAS_COMPLETE;

  constructor(public logDeltas: Array<any>) { }
}
