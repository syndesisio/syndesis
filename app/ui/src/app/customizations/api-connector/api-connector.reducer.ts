import { ActionReducerMap, createFeatureSelector } from '@ngrx/store';

import { BaseReducerModel, PlatformState } from '@syndesis/ui/platform';
import {
  ApiConnectorState,
  CustomApiConnectorRequest,
  CustomConnectorRequest
} from '@syndesis/ui/customizations/api-connector/api-connector.models';
import {
  ApiConnectorActions,
  ApiConnectorFetchComplete,
  ApiConnectorFetchFail,
  ApiConnectorCreate,
  ApiConnectorCreateComplete,
  ApiConnectorCreateCancel,
  ApiConnectorUpdate,
  ApiConnectorUpdateComplete,
  ApiConnectorUpdateFail,
  ApiConnectorDelete
} from '@syndesis/ui/customizations/api-connector/api-connector.actions';

const initialCreateRequest: CustomApiConnectorRequest = {
  connectorTemplateId: null,
  isComplete: false,
  isOK: false,
  isRequested: false
};

const initialState: ApiConnectorState = {
  list: [],
  createRequest: initialCreateRequest,
  deleted: null,
  loading: false,
  loaded: false,
  hasErrors: false,
  errors: []
};

export function apiConnectorReducer(
  state = initialState,
  action: any
): ApiConnectorState {
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
        createRequest: {
          ...createRequest,
          isComplete: false,
          isRequested: true
        },
        loading: true,
        hasErrors: false,
        errors: []
      };
    }

    case ApiConnectorActions.CREATE_COMPLETE: {
      const createRequest = (action as ApiConnectorCreateComplete).payload;
      return {
        ...state,
        createRequest: { ...createRequest, isComplete: true },
        loading: false,
        hasErrors: false,
        errors: []
      };
    }

    case ApiConnectorActions.UPDATE: {
      const updatedCustomConnection = (action as ApiConnectorUpdate).payload;
      const deleted = [...state.list].filter(
        customConnector => customConnector.id === updatedCustomConnection.id
      )[0];
      const list = [...state.list].filter(
        customConnector => customConnector.id !== updatedCustomConnection.id
      );
      list.unshift(updatedCustomConnection);

      return {
        ...state,
        list,
        deleted,
        loading: true,
        hasErrors: false,
        errors: []
      };
    }

    case ApiConnectorActions.UPDATE_COMPLETE: {
      return {
        ...state,
        deleted: null,
        loading: false,
        hasErrors: false,
        errors: []
      };
    }

    case ApiConnectorActions.DELETE: {
      const deletedConnectorId = (action as ApiConnectorDelete).payload;
      const deleted = [...state.list].filter(
        customConnector => customConnector.id === deletedConnectorId
      )[0];
      const list = [...state.list].filter(
        customConnector => customConnector.id !== deletedConnectorId
      );

      return {
        ...state,
        ...{ list },
        deleted,
        loading: false,
        hasErrors: false,
        errors: []
      };
    }

    case ApiConnectorActions.CREATE_FAIL:
    case ApiConnectorActions.UPDATE_FAIL:
    case ApiConnectorActions.DELETE_FAIL: {
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
        createRequest: initialCreateRequest,
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
          configuredProperties
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
        loaded: false,
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
        loaded: true
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

export interface ApiConnectorStore extends PlatformState {
  apiConnectorState: ApiConnectorState;
}

export const getApiConnectorState = createFeatureSelector<ApiConnectorState>(
  'apiConnectorState'
);
