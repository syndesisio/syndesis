import { ActionReducerMap, createFeatureSelector } from '@ngrx/store';

import { BaseReducerModel, PlatformStore } from '@syndesis/ui/platform';
import { IntegrationState } from './integration.models';
import * as IntegrationActions from './integration.actions';

const initialState: IntegrationState = {
  collection: [],
  loading: false,
  loaded: false,
  hasErrors: false,
  errors: []
};

export function integrationReducer(state = initialState, action: any): IntegrationState {
  switch (action.type) {

    case IntegrationActions.FETCH_INTEGRATIONS: {
      return {
        ...state,
        loading: true,
        loaded: false,
        hasErrors: false,
        errors: []
      };
    }

    case IntegrationActions.FETCH_INTEGRATIONS_COMPLETE: {
      const collection = (action as IntegrationActions.IntegrationsFetchComplete).payload;
      return {
        ...state,
        ...{ collection },
        loading: false,
        loaded: true
      };
    }

    case IntegrationActions.FETCH_INTEGRATIONS_FAIL: {
      const error = (action as IntegrationActions.IntegrationsFetchFail).payload;
      return {
        ...state,
        loading: false,
        loaded: true,
        hasErrors: true,
        errors: [error]
      };
    }

    case IntegrationActions.UPDATE_INTEGRATION: {
      const { entity, changes, loading } = action as IntegrationActions.IntegrationUpdate;
      const existingIntegration = state.collection.find(item => item.id === entity.id);
      const updatedIntegration = {
        ...existingIntegration,
        ...entity,
        ...changes
      };
      const restCollection = state.collection.filter(item => item.id !== entity.id);

      return {
        ...state,
        collection: [updatedIntegration, ...restCollection],
        inserted: updatedIntegration,
        deleted: existingIntegration,
        ...{ loading },
        hasErrors: false,
        errors: []
      };
    }

    case IntegrationActions.UPDATE_INTEGRATION_COMPLETE: {
      return {
        ...state,
        inserted: undefined,
        deleted: undefined,
        loading: false
      };
    }

    case IntegrationActions.UPDATE_INTEGRATION_FAIL: {
      const error = (action as IntegrationActions.IntegrationUpdateFail).payload;
      const deletedIntegration = state.deleted;
      const restCollection = state.collection.filter(item => item.id !== state.inserted.id);

      return {
        ...state,
        collection: [deletedIntegration, ...restCollection],
        loading: false,
        hasErrors: true,
        errors: [error]
      };
    }

    case IntegrationActions.CREATE_INTEGRATION: {
      const { entity, loading } = action as IntegrationActions.IntegrationCreate;

      return {
        ...state,
        collection: [...state.collection, entity],
        inserted: entity,
        deleted: null,
        ...{ loading },
        hasErrors: false,
        errors: []
      };
    }

    case IntegrationActions.CREATE_INTEGRATION_COMPLETE: {
      return {
        ...state,
        inserted: undefined,
        loading: false
      };
    }

    case IntegrationActions.CREATE_INTEGRATION_FAIL: {
      const error = (action as IntegrationActions.IntegrationUpdateFail).payload;
      const restCollection = state.collection.filter(item => item.id !== state.inserted.id);

      return {
        ...state,
        collection: restCollection,
        loading: false,
        hasErrors: true,
        errors: [error]
      };
    }

    case IntegrationActions.DELETE_INTEGRATION: {
      const { entity, loading } = action as IntegrationActions.IntegrationDelete;
      const restCollection = state.collection.filter(item => item.id !== entity.id);
      const deletedIntegration = state.collection.find(item => item.id === entity.id);

      return {
        ...state,
        collection: [...restCollection],
        deleted: deletedIntegration,
        ...{ loading },
        hasErrors: false,
        errors: []
      };
    }

    case IntegrationActions.DELETE_INTEGRATION_COMPLETE: {
      return {
        ...state,
        deleted: undefined,
        loading: false
      };
    }

    case IntegrationActions.DELETE_INTEGRATION_FAIL: {
      const error = (action as IntegrationActions.IntegrationDeleteFail).payload;
      const restCollection = state.collection.filter(item => item.id !== state.inserted.id);

      return {
        ...state,
        collection: [state.deleted, ...state.collection],
        loading: false,
        hasErrors: true,
        errors: [error]
      };
    }

    default: {
      return state;
    }
  }
}

// Most likely not needed since this is meant to become and app-wide state segment
export interface IntegrationStore extends PlatformStore {
  integrationState: IntegrationState;
}

export const getIntegrationState = createFeatureSelector<IntegrationState>('integrationState');
