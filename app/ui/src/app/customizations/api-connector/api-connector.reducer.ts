import { ApiConnectorState, ApiConnectors } from './api-connector.models';

import {
  ApiConnectorActions,
  ApiConnectorFetchComplete,
  ApiConnectorFetchFail
} from './api-connector.actions';

const initialState: ApiConnectorState = {
  list             : [],
  createRequest    : null,
  onSync           : true,
  isInitialized    : false,
  hasErrors        : false,
  errors           : []
};

export function apiConnectorReducer(state = initialState, action: any): ApiConnectorState {
  switch (action.type) {
    case ApiConnectorActions.FETCH: {
      return {
        ...state,
        onSync: true,
        hasErrors: false,
        errors: []
      };
    }

    case ApiConnectorActions.FETCH_COMPLETE: {
      const list = (action as ApiConnectorFetchComplete).payload;
      return {
        ...state,
        list,
        onSync: false,
        isInitialized: true,
        hasErrors: false,
        errors: []
      };
    }

    case ApiConnectorActions.FETCH_FAIL: {
      const error = (action as ApiConnectorFetchFail).payload;
      return {
        ...state,
        onSync: false,
        isInitialized: true,
        hasErrors: true,
        errors: [error]
      };
    }

    default: {
      return state;
    }
  }
}
