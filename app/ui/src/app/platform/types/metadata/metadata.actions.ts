import { Action } from '@ngrx/store';

import { MetadataState } from '@syndesis/ui/platform/types/metadata/metadata.models';

/**
 * POC: The MetadataActions class provides convenient static methods to access action
 * type tokens and helper methods to return and dispatch statically typed actions.
 * See: './metadata.reducer.ts'
 */
export const UPDATE = '[Metadata] General state update';
export const RESET = '[Metadata] State reset to initial values';

/**
 * Statically typed action classes, with constructors
 * exposing typed payload where required.
 */
export class MetadataUpdate implements Action {
  readonly type = UPDATE;

  constructor(public payload: MetadataState) {}
}

export class MetadataReset implements Action {
  readonly type = RESET;
}
