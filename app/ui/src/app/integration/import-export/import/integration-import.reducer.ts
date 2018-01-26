import { ActionReducerMap, createFeatureSelector } from '@ngrx/store';

import { BaseReducerModel, PlatformStore } from '@syndesis/ui/platform';
import { IntegrationImportState, IntegrationImportRequest, IntegrationUploadRequest } from './integration-import.models';
import * as ImportActions from './integration-import.actions';

const initialUploadRequest: IntegrationUploadRequest = {
  integrationTemplateId: null,
  isComplete: false,
  isOK: false,
  isRequested: false
};

const initialImportRequest: IntegrationImportRequest = {
  integrationTemplateId: null,
  isComplete: false,
  isOK: false,
  isRequested: false
};

const initialState: IntegrationImportState = {
  list: [],
  importRequest: initialImportRequest,
  uploadRequest: initialUploadRequest,
  loading: false,
  loaded: false,
  hasErrors: false,
  errors: []
};

export function integrationImportReducer(state = initialState, action: any): IntegrationImportState {
  switch (action.type) {

    case ImportActions.IntegrationImportActions.UPLOAD: {
      const uploadRequest = (action as ImportActions.IntegrationUpload).payload;
      return {
        ...state,
        uploadRequest: { ...uploadRequest, isComplete: false, isRequested: true },
        loading: true,
        hasErrors: false,
        errors: []
      };
    }

    case ImportActions.IntegrationImportActions.UPLOAD_COMPLETE: {
      const uploadRequest = (action as ImportActions.IntegrationUploadComplete).payload;
      return {
        ...state,
        uploadRequest: { ...uploadRequest, isComplete: true },
        loading: false,
        hasErrors: false,
        errors: []
      };
    }

    case ImportActions.IntegrationImportActions.IMPORT: {
      const importedIntegration = (action as ImportActions.IntegrationImport).payload;
      //const list = [...state.list].filter(integration => integration.id !== importedIntegration.id);
      //list.unshift(importedIntegration);

      return {
        ...state,
        //list,
        loading: true,
        hasErrors: false,
        errors: []
      };
    }

    case ImportActions.IntegrationImportActions.IMPORT_COMPLETE: {
      return {
        ...state,
        loading: false,
        hasErrors: false,
        errors: []
      };
    }

    case ImportActions.IntegrationImportActions.UPLOAD_FAIL:
    case ImportActions.IntegrationImportActions.IMPORT_FAIL: {
      return {
        ...state,
        loading: false,
        hasErrors: true,
        errors: [action.payload]
      };
    }

    case ImportActions.IntegrationImportActions.UPLOAD_CANCEL: {
      return {
        ...state,
        uploadRequest: initialUploadRequest,
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

export interface IntegrationStore extends PlatformStore {
  importState: IntegrationImportState;
}

export const getIntegrationImportState = createFeatureSelector<IntegrationImportState>('integrationImportState');
