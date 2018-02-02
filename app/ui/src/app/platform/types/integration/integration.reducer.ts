import { ActionReducerMap, createFeatureSelector } from '@ngrx/store';

import { BaseReducerModel, PlatformStore } from '@syndesis/ui/platform';
import { IntegrationRxState as IntegrationState } from './integration.models';
import * as IntegrationActions from './integration.actions';

const initialState: IntegrationState = {
  integrations: [],
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

    case IntegrationActions.FETCH_INTEGRATIONS: {
      const integrations = []; //(action as IntegrationActions.MetricsFetchComplete).payload;
      return {
        ...state,
        ...{ integrations },
        loading: false,
        loaded: true
      };
    }

    default: {
      return state;
    }
  }
}

export interface IntegrationStore extends PlatformStore {
  integrationState: IntegrationState;
}

// Mst likely not needed since this is meant to become and app-wide state segment
export const getIntegrationState = createFeatureSelector<IntegrationState>('integrationState');
