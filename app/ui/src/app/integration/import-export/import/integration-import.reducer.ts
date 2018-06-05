import { IntegrationImportState } from './integration-import.models';

import * as IntegrationImportActions from './integration-import.actions';

const initialState: IntegrationImportState = {
  file: null,
  importResults: {
    integrations: [],
    connections: []
  },
  list: [],
  loading: true,
  loaded: false,
  hasErrors: false,
  errors: []
};

export function integrationImportReducer(
  state = initialState,
  action: any
): IntegrationImportState {
  switch (action.type) {
    case IntegrationImportActions.UPLOAD_INTEGRATION: {
      return {
        ...state,
        file: (action as IntegrationImportActions.IntegrationImportUpload)
          .entity.file,
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
