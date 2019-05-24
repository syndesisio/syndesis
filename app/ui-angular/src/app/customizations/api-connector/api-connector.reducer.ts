import { createFeatureSelector, createSelector } from '@ngrx/store';

import { PlatformState } from '@syndesis/ui/platform';
import {
  ApiConnectorState,
  ApiConnectorWizardStep,
  CustomApiConnectorRequest,
  ApiConnectorData
} from '@syndesis/ui/customizations/api-connector/api-connector.models';
import {
  ApiConnectorActions,
  ApiConnectorCreate,
  ApiConnectorCreateComplete,
  ApiConnectorDelete,
  ApiConnectorFetchComplete,
  ApiConnectorFetchFail,
  ApiConnectorUpdate
} from '@syndesis/ui/customizations/api-connector/api-connector.actions';
import { OpenApiUploaderValueType, OpenApiValidationErrorMessage } from '@syndesis/ui/common';

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
  errors: [],
  uploadSpecification: {
    type: OpenApiUploaderValueType.File,
    valid: false
  },
  wizardStep: ApiConnectorWizardStep.UploadSwagger,
  showApiEditor: false,
  specificationForEditor: undefined
};

export function nextStep( state: ApiConnectorState ): ApiConnectorState {
  switch ( state.wizardStep ) {
    case ApiConnectorWizardStep.UploadSwagger:
      return {
        ...state,
        wizardStep: ApiConnectorWizardStep.ReviewApiConnector
      };

    case ApiConnectorWizardStep.ReviewApiConnector:
      return {
        ...state,
        wizardStep: ApiConnectorWizardStep.UpdateAuthSettings
      };

    case ApiConnectorWizardStep.UpdateAuthSettings:
      return {
        ...state,
        wizardStep: ApiConnectorWizardStep.SubmitRequest
      };

    default:
      return state;
  }
}

export function previousStep( state: ApiConnectorState ): ApiConnectorState {
  switch ( state.wizardStep ) {
    case ApiConnectorWizardStep.ReviewApiConnector:
      return {
        ...state,
        wizardStep: ApiConnectorWizardStep.UploadSwagger,
        specificationForEditor: undefined
      };

    case ApiConnectorWizardStep.UpdateAuthSettings:
      return {
        ...state,
        wizardStep: ApiConnectorWizardStep.ReviewApiConnector
      };

    case ApiConnectorWizardStep.SubmitRequest:
      return {
        ...state,
        wizardStep: ApiConnectorWizardStep.UpdateAuthSettings
      };

    default:
      return state;
  }
}

export function apiConnectorReducer(
  state = initialState,
  action: any
): ApiConnectorState {
  switch (action.type) {
    case ApiConnectorActions.NEXT_STEP: {
      return nextStep( state );
    }

    case ApiConnectorActions.PREV_STEP: {
      return previousStep( state );
    }

    case ApiConnectorActions.UPLOAD_SPEC: {
      return {
        ...state,
        // wizardStep: ApiConnectorWizardStep.UploadSwagger,
        uploadSpecification: action.payload,
        validationErrors: null,
        createRequest: {
          ...state.createRequest,
          connectorTemplateId: 'swagger-connector-template',
          errors: [],
          warnings: [],
          specificationFile: action.payload.type == OpenApiUploaderValueType.File
            ? action.payload.spec as File
            : null,
          configuredProperties: {
            specification: action.payload.type == OpenApiUploaderValueType.Url
              ? action.payload.spec as string
              : null
          }
        }
      };
    }

    case ApiConnectorActions.EDIT_SPEC: {
      return {
        ...state,
        showApiEditor: true,
        specificationForEditor:
          state.uploadSpecification.type === OpenApiUploaderValueType.Spec
            ? state.uploadSpecification.spec as string
            : state.specificationForEditor
      };
    }

    case ApiConnectorActions.CANCEL_EDIT_SPEC: {
      return {
        ...state,
        showApiEditor: false
      };
    }

    case ApiConnectorActions.UPDATE_SPEC: {
      return {
        ...state,
        createRequest: {
          ...state.createRequest,
          actionsSummary: null,
          configuredProperties: {
            ...state.createRequest.configuredProperties,
            specification: action.payload
          },
          errors: [],
          warnings: [],
          specificationFile: null,
          name: action.payload.info ? action.payload.info.title ? action.payload.info.title
                                                                : 'No title provided'
                                    : 'No title provided',
          description: action.payload.info ? action.payload.info.description ? action.payload.info.description
                                                                             : 'No description provided'
                                           : 'No description provided',
        },
        wizardStep: ApiConnectorWizardStep.ReviewApiConnector,
        showApiEditor: false,
        specificationForEditor: action.payload
      };
    }

    case ApiConnectorActions.VALIDATE_SWAGGER: {
      return {
        ...state,
        createRequest : {
          ...state.createRequest,
          name: null,
          description: null
        },
        loading: true,
        hasErrors: false,
        errors: []
      };
    }

    case ApiConnectorActions.SET_CONNECTOR_DATA: {
      return {
        ...state,
        createRequest: {
          ...state.createRequest,
          id: action.payload.id,
          actionsSummary: action.payload.actionsSummary,
          name: action.payload.name,
          description: action.payload.description,
          warnings: action.payload.warnings,
          errors: action.payload.errors,
          icon: action.payload.icon,
          configuredProperties: action.payload.configuredProperties,
          properties: action.payload.properties
        }
      };
    }

    case ApiConnectorActions.VALIDATE_SWAGGER_COMPLETE: {
      return {
        ...state,
        createRequest: {
          ...state.createRequest,
          actionsSummary: action.payload.actionsSummary,
          configuredProperties: action.payload.configuredProperties,
          properties: action.payload.properties,
          warnings: action.payload.warnings,
          errors: action.payload.errors,
          name: action.payload.name,
          description: action.payload.description
        },
        loading: false,
        hasErrors: false,
        errors: action.payload.errors,
        specificationForEditor: action.payload.configuredProperties ? action.payload.configuredProperties.specification : null
      };
    }

    case ApiConnectorActions.VALIDATE_SWAGGER_FAIL: {
      const response = action.payload as ApiConnectorData;
      return {
        ...state,
        wizardStep: ApiConnectorWizardStep.UploadSwagger,
        loading: false,
        hasErrors: true,
        validationErrors: response.errors
      };
    }

    case ApiConnectorActions.CREATE: {
      const request = (action as ApiConnectorCreate).payload;
      return {
        ...state,
        createRequest: {
          ...request,
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
        ...initialState
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

export const getApiConnectorLoading = createSelector(
  getApiConnectorState,
  (state: ApiConnectorState) => state.loading
);

export const getApiConnectorSpecificationForEditor = createSelector(
  getApiConnectorState,
  (state: ApiConnectorState): string => state.specificationForEditor
);

export const getApiConnectorSpecificationForValidation = createSelector(
  getApiConnectorState,
  (state: ApiConnectorState) => {
    return state.createRequest;
  }
);

export const getApiConnectorUploadSpecification = createSelector(
  getApiConnectorState,
  (state: ApiConnectorState) => state.uploadSpecification
);

export const getApiConnectorValidationError = createSelector(
  getApiConnectorState,
  (state: ApiConnectorState): OpenApiValidationErrorMessage[] => state.validationErrors
);

export const getApiConnectorWizardStep = createSelector(
  getApiConnectorState,
  ( state: ApiConnectorState ) => state.wizardStep
);

export const getApiConnectorRequest = createSelector(
  getApiConnectorState,
  ( state: ApiConnectorState ) => state.createRequest
);

export const getShowApiEditor = createSelector(
  getApiConnectorState,
  ( state: ApiConnectorState ) => state.showApiEditor
);
