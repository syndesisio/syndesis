import { createFeatureSelector } from '@ngrx/store';

import { PlatformState } from '@syndesis/ui/platform';
import {
  ApiProviderState,
} from '@syndesis/ui/integration/api-provider/api-provider.models';
import {
  ApiProviderActions
} from '@syndesis/ui/integration/api-provider/api-provider.actions';

const initialState: ApiProviderState = {
  loading: false,
  loaded: false,
  hasErrors: false,
  errors: [],
  createRequest: {}
};

export function apiProviderReducer(
  state = initialState,
  action: any
): ApiProviderState {
  switch (action.type) {
    case ApiProviderActions.VALIDATE_SWAGGER: {
      return {
        ...state,
        uploadSpecification: action.payload,
        createRequest: {},
        loading: true,
        hasErrors: false,
        errors: []
      };
    }

    case ApiProviderActions.VALIDATE_SWAGGER_COMPLETE: {
      const { errors, ...createRequest } = action.payload;
      return {
        ...state,
        createRequest,
        loading: false,
        hasErrors: !!errors,
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

    case ApiProviderActions.CREATE_CANCEL: {
      return {
        ...state,
        createRequest: {},
        loading: false,
        hasErrors: false,
        errors: []
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
