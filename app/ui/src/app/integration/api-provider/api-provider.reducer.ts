import { createFeatureSelector, createSelector } from '@ngrx/store';

import { PlatformState } from '@syndesis/ui/platform';
import { ApiProviderState, ApiProviderWizardSteps } from '@syndesis/ui/integration/api-provider/api-provider.models';
import { ApiProviderActions } from '@syndesis/ui/integration/api-provider/api-provider.actions';
import {
  OpenApiUploaderValue,
  OpenApiUploaderValueType,
  OpenApiValidationErrorMessage,
  OpenApiValidationResponse
} from '@syndesis/ui/common';

const initialState: ApiProviderState = {
  loading: false,
  loaded: false,
  hasErrors: false,
  uploadSpecification: {
    type: OpenApiUploaderValueType.File,
    valid: false
  },
  wizardStep: ApiProviderWizardSteps.UploadSpecification,
  specificationForEditor: null
};

export function nextStep(state: ApiProviderState): ApiProviderState {
  switch (state.wizardStep) {
    case ApiProviderWizardSteps.UploadSpecification: {
      return {
        ...state,
        wizardStep:
          state.uploadSpecification.type === OpenApiUploaderValueType.Spec ?
            ApiProviderWizardSteps.EditSpecification :
            ApiProviderWizardSteps.ReviewApiProvider,
        specificationForEditor:
          state.uploadSpecification.type === OpenApiUploaderValueType.Spec ?
            state.uploadSpecification.spec as string :
            state.specificationForEditor
      };
    }

    default: {
      return state;
    }
  }
}

export function previousStep(state: ApiProviderState): ApiProviderState {
  switch (state.wizardStep) {
    case ApiProviderWizardSteps.ReviewApiProvider:
      return {
        ...state,
        wizardStep: ApiProviderWizardSteps.UploadSpecification,
        specificationForEditor: null
      };

    case ApiProviderWizardSteps.EditSpecification:
      return {
        ...state,
        wizardStep: ApiProviderWizardSteps.ReviewApiProvider
      };

    default:
      return state;
  }
}

export function apiProviderReducer(
  state = initialState,
  action: any
): ApiProviderState {
  switch (action.type) {
    case ApiProviderActions.NEXT_STEP: {
      return nextStep(state);
    }

    case ApiProviderActions.PREV_STEP: {
      return previousStep(state);
    }

    case ApiProviderActions.UPLOAD_SPEC: {
      return {
        ...state,
        uploadSpecification: action.payload,
      };
    }

    case ApiProviderActions.VALIDATE_SPEC: {
      return {
        ...state,
        loading: true,
        hasErrors: false,
      };
    }

    case ApiProviderActions.VALIDATE_SPEC_COMPLETE: {
      return {
        ...state,
        validationResponse: action.payload,
        validationErrors: null,
        loading: false,
        hasErrors: false,
        wizardStep: ApiProviderWizardSteps.ReviewApiProvider,
        specificationForEditor: (action.payload as OpenApiValidationResponse).configuredProperties.specification
      };
    }

    case ApiProviderActions.VALIDATE_SPEC_FAIL: {
      return {
        ...state,
        loading: false,
        hasErrors: true,
        validationResponse: null,
        validationErrors: action.payload,
        wizardStep: ApiProviderWizardSteps.UploadSpecification,
        specificationForEditor: null
      };
    }

    case ApiProviderActions.EDIT_SPEC: {
      return {
        ...state,
        wizardStep: ApiProviderWizardSteps.EditSpecification
      };
    }

    case ApiProviderActions.UPDATE_SPEC: {
      return {
        ...state,
        wizardStep: ApiProviderWizardSteps.ReviewApiProvider,
        specificationForEditor: action.payload
      };
    }

    case ApiProviderActions.CREATE_CANCEL: {
      return {
        ...initialState
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

export const getApiProviderWizardStep = createSelector(
  getApiProviderState,
  (state: ApiProviderState) => state.wizardStep
);

export const getApiProviderUploadSpecification = createSelector(
  getApiProviderState,
  (state: ApiProviderState) => state.uploadSpecification
);

export const getApiProviderValidationError = createSelector(
  getApiProviderState,
  (state: ApiProviderState): OpenApiValidationErrorMessage[] => state.validationErrors && state.validationErrors.errors
);

export const getApiProviderValidationResponse = createSelector(
  getApiProviderState,
  (state: ApiProviderState) => state.validationResponse
);

export const getApiProviderValidationLoading = createSelector(
  getApiProviderState,
  (state: ApiProviderState) => state.loading
);

export const getApiProviderValidationLoaded = createSelector(
  getApiProviderState,
  (state: ApiProviderState) => state.loaded
);

export const getApiProviderSpecificationForEditor = createSelector(
  getApiProviderState,
  (state: ApiProviderState): string => state.specificationForEditor
);

export const getApiProviderSpecificationForValidation = createSelector(
  getApiProviderState,
  (state: ApiProviderState): OpenApiUploaderValue =>
    state.specificationForEditor || state.uploadSpecification.spec
);
