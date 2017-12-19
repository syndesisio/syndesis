import { Action } from '@ngrx/store';

import { MetadataState } from './metadata.models';

/**
 * POC: The MetadataActions class provides convenient static methods to access action
 * type tokens and helper methods to return and dispatch statically typed actions.
 * See: './metadata.reducer.ts'
 */
export class MetadataActions {
  // Action type tokens
  static UPDATE    = '[Metadata] General state update';
  static RESET     = '[Metadata] State reset to initial values';

  // Static helper methods to conveniently call and return typed actions
  // when executing Store.dispatch(). Match with Action classes below.
  static update(payload: MetadataState) {
    return new MetadataUpdate(payload);
  }

  static reset() {
    return new MetadataReset();
  }
}

/**
 * Statically typed action classes, with constructors
 * exposing typed payload where required.
 */
export class MetadataUpdate implements Action {
  readonly type = MetadataActions.UPDATE;

  constructor(public payload: MetadataState) { }
}

export class MetadataReset implements Action {
  readonly type = MetadataActions.RESET;
}
