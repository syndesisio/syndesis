import { ActionReducerMap } from '@ngrx/store';

import { PlatformStore } from './platform.store';
import { metadataReducer } from './metadata';

export const platformReducer: ActionReducerMap<PlatformStore> = {
  metadataState: metadataReducer,
  // Add new [tokenizedState: stateReducer] mappings below...
};
