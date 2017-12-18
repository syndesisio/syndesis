import { ActionReducerMap, createFeatureSelector } from '@ngrx/store';

import { BaseReducerModel, PlatformStore } from '@syndesis/ui/platform';
import { ApiConnectorState } from './api-connector.models';
import {
  ApiConnectorActions,
  ApiConnectorFetchComplete,
  ApiConnectorFetchFail
} from './api-connector.actions';


const initialState: ApiConnectorState = {
  list             : [],
  createRequest    : null,
  loading          : true,
  loaded           : false,
  hasErrors        : false,
  errors           : []
};

export function apiConnectorReducer(state = initialState, action: any): ApiConnectorState {
  switch (action.type) {
    case ApiConnectorActions.FETCH: {
      return {
        ...state,
        loading: true,
        hasErrors: false,
        errors: []
      };
    }

    case ApiConnectorActions.FETCH_COMPLETE: {
      const list = (action as ApiConnectorFetchComplete).payload;
      return {
        ...state,
        list,
        loading: false,
        loaded: true,
        hasErrors: false,
        errors: []
      };
    }

    case ApiConnectorActions.FETCH_FAIL: {
      const error = (action as ApiConnectorFetchFail).payload;
      return {
        ...state,
        loading: false,
        loaded: true,
        hasErrors: true,
        errors: [error]
      };
    }

    default: {
      return state;
    }
  }
}

export interface ApiConnectorStore extends PlatformStore {
  apiConnectorState: ApiConnectorState;
}

export const getApiConnectorState = createFeatureSelector<ApiConnectorState>('apiConnectorState');
