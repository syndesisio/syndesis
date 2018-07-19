import { Action } from '@ngrx/store';

import { BaseEntity, ActionReducerError } from '@syndesis/ui/platform';
import {
  Integration,
  Integrations,
  IntegrationMetrics,
  IntegrationOverviews
} from '@syndesis/ui/platform/types/integration/integration.models';

export const FETCH_INTEGRATIONS = '[Integrations] Fetch integrations request';
export const FETCH_INTEGRATIONS_COMPLETE =
  '[Integrations] Fetch integrations complete';
export const FETCH_INTEGRATIONS_FAIL =
  '[Integrations] Fetch integrations failed';

export const REFRESH_OVERVIEWS = '[Integrations] Fetch integration overviews';
export const REFRESH_OVERVIEWS_FAIL =
  '[Integrations] Fetch integrations overviews failed';

export const UPDATE_INTEGRATION = '[Integrations] Update integration';
export const UPDATE_INTEGRATION_COMPLETE =
  '[Integrations] Updated integration now synchronized';
export const UPDATE_INTEGRATION_FAIL =
  '[Integrations] Updated integration sync failed';

export const CREATE_INTEGRATION = '[Integrations] New Integration created';
export const CREATE_INTEGRATION_COMPLETE =
  '[Integrations] New Integration now synchronized';
export const CREATE_INTEGRATION_FAIL =
  '[Integrations] New Integrarion sync failed';

export const DELETE_INTEGRATION = '[Integrations] Delete integration';
export const DELETE_INTEGRATION_COMPLETE =
  '[Integrations] Deleted integration now synchronized';
export const DELETE_INTEGRATION_FAIL =
  '[Integrations] Deleted integration sync failed';

export const FETCH_METRICS = '[Integrations] Fetch metrics request';
export const FETCH_METRICS_COMPLETE = '[Integrations] Fetch metrics complete';
export const FETCH_METRICS_FAIL = '[Integrations] Fetch metrics failed';

export class IntegrationsFetch implements Action {
  readonly type = FETCH_INTEGRATIONS;
}

export class IntegrationsFetchComplete implements Action {
  readonly type = FETCH_INTEGRATIONS_COMPLETE;

  constructor(public payload: Integrations) {}
}

export class IntegrationsFetchFail implements Action {
  readonly type = FETCH_INTEGRATIONS_FAIL;

  constructor(public payload: ActionReducerError) {}
}

export class IntegrationsRefreshOverviews implements Action {
  readonly type = REFRESH_OVERVIEWS;

  constructor(public payload: IntegrationOverviews) {}
}

export class IntegrationsRefreshOverviewsFail implements Action {
  readonly type = REFRESH_OVERVIEWS_FAIL;

  constructor(public payload: ActionReducerError) {}
}

export class IntegrationUpdate implements Action {
  readonly type = UPDATE_INTEGRATION;

  constructor(
    public entity: Integration | BaseEntity,
    public changes: any,
    public loading = false
  ) {}
}

export class IntegrationUpdateComplete implements Action {
  readonly type = UPDATE_INTEGRATION_COMPLETE;
}

export class IntegrationUpdateFail implements Action {
  readonly type = UPDATE_INTEGRATION_FAIL;

  constructor(public payload: ActionReducerError) {}
}

export class IntegrationCreate implements Action {
  readonly type = CREATE_INTEGRATION;

  constructor(public entity: Integration, public loading = false) {}
}

export class IntegrationCreateComplete implements Action {
  readonly type = CREATE_INTEGRATION_COMPLETE;
}

export class IntegrationCreateFail implements Action {
  readonly type = CREATE_INTEGRATION_FAIL;

  constructor(public payload: ActionReducerError) {}
}

export class IntegrationDelete implements Action {
  readonly type = DELETE_INTEGRATION;

  constructor(
    public entity: Integration | BaseEntity,
    public loading = false
  ) {}
}

export class IntegrationDeleteComplete implements Action {
  readonly type = DELETE_INTEGRATION_COMPLETE;
}

export class IntegrationDeleteFail implements Action {
  readonly type = DELETE_INTEGRATION_FAIL;

  constructor(public payload: ActionReducerError) {}
}

export class FetchMetrics implements Action {
  readonly type = FETCH_METRICS;

  constructor(public id?: string) {}
}

export class FetchMetricsComplete implements Action {
  readonly type = FETCH_METRICS_COMPLETE;

  constructor(public payload: IntegrationMetrics) {}
}

export class FetchMetricsFailure implements Action {
  readonly type = FETCH_METRICS_FAIL;

  constructor(public payload: ActionReducerError) {}
}
