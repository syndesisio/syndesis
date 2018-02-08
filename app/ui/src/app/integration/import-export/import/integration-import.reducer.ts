import { PlatformStore } from '@syndesis/ui/platform';
import { ActionReducerMap } from '@ngrx/store';

import {
  IntegrationImportEditState,
  IntegrationImportsEditState,
  IntegrationImportUploadState,
  IntegrationImportsUploadState
} from './integration-import.models';

import * as IntegrationImportActions from './integration-import.actions';

const initialState: IntegrationImportUploadState = {
  file: null,
  importResults: {
    integrations: [],
    connections: [],
  },
  list: [],
  loading: true,
  loaded: false,
  hasErrors: false,
  errors: []
};

export function integrationImportUploadReducer(state = initialState, action: any): IntegrationImportUploadState {
  switch (action.type) {

    case IntegrationImportActions.UPLOAD_INTEGRATION: {
      const request = (action as IntegrationImportActions.IntegrationImportUpload).payload;
      return {
        ...state,
        file: request.file,
        loading: true,
        loaded: false,
        hasErrors: false,
        errors: []
      };
    }

    default: {
      return state;
    }
  }
}

