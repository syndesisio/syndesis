import { Action } from '@ngrx/store';

import { Connections } from './connection.model';

export const ActionTypes = {
  LOAD: '[Connection] Load',
  LOAD_SUCCESS: '[Connection] Load Success',
  LOAD_FAILURE: '[Connection] Load Failure',
};

export class LoadAction implements Action {
  type = ActionTypes.LOAD;
  payload?: any;

  constructor() { }
}

export class LoadSuccessAction implements Action {
  type = ActionTypes.LOAD_SUCCESS;

  constructor(public payload: Connections) { }
}

export class LoadFailureAction implements Action {
  type = ActionTypes.LOAD_FAILURE;

  constructor(public payload: any) { }
}

export type Actions
  = LoadAction
  | LoadSuccessAction
  | LoadFailureAction;
