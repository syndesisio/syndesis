import { MetadataState } from './metadata.models';

import { MetadataActions } from './metadata.actions';

const initialState: MetadataState = {
  appName          : 'Syndesis',
  locale           : 'en-us',
  onSync           : false,
  isInitialized    : true,
  hasErrors        : false,
  errors           : []
};

export function metadataReducer(state = initialState, action: any): MetadataState {
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
