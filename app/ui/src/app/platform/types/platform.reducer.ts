/**
 * PlatformState defines the contract scheme for the overall application store,
 * available to all feature domains, either eagerly or lazy loaded. For each
 * new slice of state defined at @syndesis/ui/platform, add the corresponding
 * state key and its interface type at PlatformState, plus the same state key
 * associated to its particular reducer to the platformReducer object literal.
 * This reducer will be made available later to lazy loaded modules within their
 * own fractal state store thru store composition, store inheritance and custom
 * feature selectors. You can also create convenient memoized state selctor pointers
 * at the bottom of the file.
 */
import { ActionReducerMap } from '@ngrx/store';

import { MetadataState, metadataReducer } from '@syndesis/ui/platform/types/metadata';
import { IntegrationState, integrationReducer } from '@syndesis/ui/platform/types/integration';
import { I18NState, i18nReducer } from '@syndesis/ui/platform/types/i18n';

export interface PlatformState {
  metadataState: MetadataState;
  integrationState: IntegrationState;
  i18nState: I18NState;
  // Add any new [tokenizedState: stateModelInterface] mapping below...
}

export const platformReducer: ActionReducerMap<PlatformState> = {
  metadataState: metadataReducer,
  integrationState: integrationReducer,
  i18nState: i18nReducer
  // Add any new [tokenizedState: stateReducer] mapping below...
};
