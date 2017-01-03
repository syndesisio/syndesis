import { Action } from '@ngrx/store';

import { Templates } from './template.model';

export const ActionTypes = {
  LOAD: '[Template] Load',
  LOAD_SUCCESS: '[Template] Load Success',
  LOAD_FAILURE: '[Template] Load Failure',
};

export class LoadAction implements Action {
  type = ActionTypes.LOAD;
  payload?: any;

  constructor() { }
}

export class LoadSuccessAction implements Action {
  type = ActionTypes.LOAD_SUCCESS;

  constructor(public payload: Templates) { }
}

export class LoadFailureAction implements Action {
  type = ActionTypes.LOAD_FAILURE;

  constructor(public payload: any) { }
}

export type Actions
  = LoadAction
  | LoadSuccessAction
  | LoadFailureAction;
