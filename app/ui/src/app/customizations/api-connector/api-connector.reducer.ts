import { ActionReducerMap, createFeatureSelector } from '@ngrx/store';

import { BaseReducerModel, PlatformStore } from '@syndesis/ui/platform';
import { ApiConnectorState } from './api-connector.models';
import {
  ApiConnectorActions,
  ApiConnectorFetchComplete,
  ApiConnectorFetchFail,
  ApiConnectorCreate,
  ApiConnectorCreateComplete,
  ApiConnectorCreateCancel,
} from './api-connector.actions';

const initialState: ApiConnectorState = {
  list             : [],
  createRequest    : null,
  loading          : false,
  loaded           : false,
  hasErrors        : false,
  errors           : []
};

export function apiConnectorReducer(state = initialState, action: any): ApiConnectorState {
  switch (action.type) {
    case ApiConnectorActions.VALIDATE_SWAGGER: {
      return {
        ...state,
        createRequest: action.payload,
        loading: true,
        hasErrors: false,
        errors: []
      };
    }

    case ApiConnectorActions.VALIDATE_SWAGGER_COMPLETE: {
      const { validationDetails, errors } = action.payload;
      return {
        ...state,
        createRequest: {
          ...state.createRequest,
          ...action.payload,
          validationDetails: validationDetails
        },
        loading: false,
        hasErrors: false,
        errors: errors
      };
    }

    case ApiConnectorActions.VALIDATE_SWAGGER_FAIL: {
      return {
        ...state,
        loading: false,
        hasErrors: true,
        errors: [action.payload]
      };
    }

    case ApiConnectorActions.CREATE: {
      const createRequest = (action as ApiConnectorCreate).payload;
      return {
        ...state,
        createRequest,
        loading: true,
        hasErrors: false,
        errors: []
      };
    }

    case ApiConnectorActions.CREATE_COMPLETE: {
      const createRequest = (action as ApiConnectorCreateComplete).payload;
      return {
        ...state,
        createRequest,
        loading: false,
        hasErrors: false,
        errors: []
      };
    }

    case ApiConnectorActions.CREATE_FAIL: {
      return {
        ...state,
        loading: false,
        hasErrors: true,
        errors: [action.payload]
      };
    }

    case ApiConnectorActions.CREATE_CANCEL: {
      return {
        ...state,
        createRequest: undefined,
        loading: false,
        hasErrors: false,
        errors: []
      };
    }

    case ApiConnectorActions.UPDATE_AUTH_SETTINGS: {
      const configuredProperties = {
        ...state.createRequest.configuredProperties,
        ...action.payload
      };
      return {
        ...state,
        createRequest: {
          ...state.createRequest,
          configuredProperties,
        },
        loading: false,
        hasErrors: false,
        errors: []
      };
    }

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
