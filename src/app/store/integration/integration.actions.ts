import { Action } from '@ngrx/store';

import { Integrations } from './integration.model';

export const ActionTypes = {
  LOAD: '[Integration] Load',
  LOAD_SUCCESS: '[Integration] Load Success',
  LOAD_FAILURE: '[Integration] Load Failure',
};

export class LoadAction implements Action {
  type = ActionTypes.LOAD;
  payload?: any;

  constructor() { }
}

export class LoadSuccessAction implements Action {
  type = ActionTypes.LOAD_SUCCESS;

  constructor(public payload: Integrations) { }
}

export class LoadFailureAction implements Action {
  type = ActionTypes.LOAD_FAILURE;

  constructor(public payload: any) { }
}

export type Actions
  = LoadAction
  | LoadSuccessAction
  | LoadFailureAction;
