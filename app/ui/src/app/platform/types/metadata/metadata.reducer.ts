/**
 * POC: the implementation below serves as a blueprint for coding slices of state
 * consumed by any application domain, either eagerly or lazy loaded. It contains
 * the usual implementation of a default initial state and associated reducer,
 * plus a convenience state slice selector at the bottom.
 * See: './../platform.reducer.ts'
 */
import { createFeatureSelector } from '@ngrx/store';

import { MetadataState } from '@syndesis/ui/platform/types/metadata/metadata.models';
import * as MetadataActions from '@syndesis/ui/platform/types/metadata/metadata.actions';

const initialState: MetadataState = {
  appName: 'Syndesis',
  locale: 'en-us',
  loading: false,
  loaded: true,
  hasErrors: false,
  errors: []
};

export function metadataReducer(
  state = initialState,
  action: any
): MetadataState {
  switch (action.type) {
    case MetadataActions.UPDATE: {
      return {
        ...state,
        ...action.payload
      };
    }

    case MetadataActions.RESET: {
      return initialState;
    }

    default: {
      return state;
    }
  }
}

export const selectMetadataState = createFeatureSelector<MetadataState>(
  'metadataState'
);
