import { createFeatureSelector } from '@ngrx/store';

import { PlatformState } from '@syndesis/ui/platform';
import {
  ApiProviderState,
  CustomApiProviderRequest
} from '@syndesis/ui/integration/api-provider/api-provider.models';
import {
  ApiProviderActions
} from '@syndesis/ui/integration/api-provider/api-provider.actions';

const initialCreateRequest: CustomApiProviderRequest = {
  connectorTemplateId: null,
  isComplete: false,
  isOK: false,
  isRequested: false
};

const initialState: ApiProviderState = {
  list: [],
  createRequest: initialCreateRequest,
  deleted: null,
  loading: false,
  loaded: false,
  hasErrors: false,
  errors: []
};

export function apiProviderReducer(
  state = initialState,
  action: any
): ApiProviderState {
  switch (action.type) {
    case ApiProviderActions.VALIDATE_SWAGGER: {
      return {
        ...state,
        createRequest: action.payload,
        loading: true,
        hasErrors: false,
        errors: []
      };
    }

    case ApiProviderActions.VALIDATE_SWAGGER_COMPLETE: {
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

    case ApiProviderActions.VALIDATE_SWAGGER_FAIL: {
      return {
        ...state,
        loading: false,
        hasErrors: true,
        errors: [action.payload]
      };
    }

    default: {
      return state;
    }
  }
}

export interface ApiProviderStore extends PlatformState {
  apiProviderState: ApiProviderState;
}

export const getApiProviderState = createFeatureSelector<ApiProviderState>(
  'apiProviderState'
);
