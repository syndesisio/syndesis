/**
 * PlatformStore defines the contract scheme for the overall application store,
 * available to all feature domains, either eagerly or lazy loaded. For each
 * new slice of state defined at @syndesis/ui/platform, add the corresponding
 * state key and its interface type at PlatformStore, plus the same state key
 * associated to its particular reducer. This reducer will be made available
 * later to lazy loaded modules within their own fractal state store thru
 * store composition, store inheritance and custom feature selectors.
 */
import { ActionReducerMap } from '@ngrx/store';

import { MetadataState, metadataReducer } from './metadata';
import { IntegrationState, integrationReducer } from './integration';

export interface PlatformStore {
  metadataState: MetadataState;
  integrationState: IntegrationState;
  // Add any new [tokenizedState: stateModelInterface] mapping below...
}

export const platformReducer: ActionReducerMap<PlatformStore> = {
  metadataState: metadataReducer,
  integrationState: integrationReducer,
  // Add any new [tokenizedState: stateReducer] mapping below...
};
