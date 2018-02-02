import { Action } from '@ngrx/store';

import { ActionReducerError, Integration } from '@syndesis/ui/platform';

export const FETCH_INTEGRATIONS            = '[Integrations] Fetch integrations request';
export const FETCH_INTEGRATIONS_COMPLETE   = '[Integrations] Fetch integrations complete';
export const FETCH_INTEGRATIONS_FAIL       = '[Integrations] Fetch integrations failed';
export const UPDATE_INTEGRATION            = '[Integrations] Update integration';
export const UPDATE_INTEGRATION_COMPLETE   = '[Integrations] Updated integration syncd';
export const UPDATE_INTEGRATION_FAIL       = '[Integrations] Update integration sync failed';

export const CREATE_INTEGRATION            = '[Integrations] New Integration created';
export const CREATE_INTEGRATION_FAIL       = '[Integrations] New Integrarion sync failed';

export const DELETE_INTEGRATION            = '[Integrations] New Integration created';
export const DELETE_INTEGRATION_FAIL       = '[Integrations] New Integrarion sync failed';

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
