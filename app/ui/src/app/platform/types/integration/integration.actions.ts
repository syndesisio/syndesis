import { Action } from '@ngrx/store';

import { BaseEntity, ActionReducerError, Integration, Integrations } from '@syndesis/ui/platform';

export const FETCH_INTEGRATIONS            = '[Integrations] Fetch integrations request';
export const FETCH_INTEGRATIONS_COMPLETE   = '[Integrations] Fetch integrations complete';
export const FETCH_INTEGRATIONS_FAIL       = '[Integrations] Fetch integrations failed';

export const UPDATE_INTEGRATION            = '[Integrations] Update integration';
export const UPDATE_INTEGRATION_COMPLETE   = '[Integrations] Updated integration now synchronized';
export const UPDATE_INTEGRATION_FAIL       = '[Integrations] Updated integration sync failed';

export const CREATE_INTEGRATION            = '[Integrations] New Integration created';
export const CREATE_INTEGRATION_COMPLETE   = '[Integrations] New Integration now synchronized';
export const CREATE_INTEGRATION_FAIL       = '[Integrations] New Integrarion sync failed';

export const DELETE_INTEGRATION            = '[Integrations] Delete integration';
export const DELETE_INTEGRATION_COMPLETE   = '[Integrations] Deleted integration now synchronized';
export const DELETE_INTEGRATION_FAIL       = '[Integrations] Deleted integration sync failed';

export class IntegrationsFetch implements Action {
  readonly type = FETCH_INTEGRATIONS;
}

export class IntegrationsFetchComplete implements Action {
  readonly type = FETCH_INTEGRATIONS_COMPLETE;

  constructor(public payload: Integrations) { }
}

export class IntegrationsFetchFail implements Action {
  readonly type = FETCH_INTEGRATIONS_FAIL;

  constructor(public payload: ActionReducerError) { }
}

export class IntegrationUpdate implements Action {
  readonly type = UPDATE_INTEGRATION;

  constructor(public entity: Integration | BaseEntity, public changes: any, public loading = false) { }
}

export class IntegrationUpdateComplete implements Action {
  readonly type = UPDATE_INTEGRATION_COMPLETE;
}

export class IntegrationUpdateFail implements Action {
  readonly type = UPDATE_INTEGRATION_FAIL;

  constructor(public payload: ActionReducerError) { }
}

export class IntegrationCreate implements Action {
  readonly type = CREATE_INTEGRATION;

  constructor(public entity: Integration, public loading = false) { }
}

export class IntegrationCreateComplete implements Action {
  readonly type = CREATE_INTEGRATION_COMPLETE;
}

export class IntegrationCreateFail implements Action {
  readonly type = CREATE_INTEGRATION_FAIL;

  constructor(public payload: ActionReducerError) { }
}

export class IntegrationDelete implements Action {
  readonly type = DELETE_INTEGRATION;

  constructor(public entity: Integration | BaseEntity, public loading = false) { }
}

export class IntegrationDeleteComplete implements Action {
  readonly type = DELETE_INTEGRATION_COMPLETE;
}

export class IntegrationDeleteFail implements Action {
  readonly type = DELETE_INTEGRATION_FAIL;

  constructor(public payload: ActionReducerError) { }
}


//* ******************************************************* 
// TEMP list of matches b/w actions and legacy methods in Store
//******************************************************** */

// FETCH
// loadAll()/.list

// UPDATE:
// activate(integration)
// deactivate(integration)
// update(integration)

// CREATE:
// create - formerly newInstance

// DELETE:
// delete(integration)

// ????
// check store.updateOrCreate 
// check store.loadOrCreate
