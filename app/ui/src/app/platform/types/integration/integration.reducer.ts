import { createFeatureSelector } from '@ngrx/store';

import { IntegrationState, IntegrationMetrics } from '@syndesis/ui/platform/types/integration/integration.models';
import * as IntegrationActions from '@syndesis/ui/platform/types/integration/integration.actions';

const initialIntegrationMetrics: IntegrationMetrics = {
  messages: 0,
  errors: 0,
  start: Date.now(),
  lastProcessed: Date.now(),
  topIntegrations: {}
};

const initialState: IntegrationState = {
  collection: [],
  metrics: {
    summary: initialIntegrationMetrics,
    list: []
  },
  loading: false,
  loaded: false,
  hasErrors: false,
  errors: []
};

export function integrationReducer(
  state = initialState,
  action: any
): IntegrationState {
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
      const collection =
        (action as IntegrationActions.IntegrationsFetchComplete).payload || [];
      return {
        ...state,
        ...{ collection },
        loading: false,
        loaded: true
      };
    }

    case IntegrationActions.REFRESH_OVERVIEWS: {
      const overviews =
        (action as IntegrationActions.IntegrationsRefreshOverviews).payload ||
        [];
      const collection = [...state.collection].map(integration => {
        const integrationOverview = overviews.find(
          overview => overview.id === integration.id
        );
        return { ...integration, ...integrationOverview };
      });

      return {
        ...state,
        ...{ collection },
        loading: false,
        loaded: true
      };
    }

    case IntegrationActions.REFRESH_OVERVIEWS_FAIL:
    case IntegrationActions.FETCH_METRICS_FAIL:
    case IntegrationActions.FETCH_INTEGRATIONS_FAIL: {
      const error = (action as IntegrationActions.IntegrationsFetchFail)
        .payload;
      return {
        ...state,
        loading: false,
        loaded: true,
        hasErrors: true,
        errors: [error]
      };
    }

    case IntegrationActions.UPDATE_INTEGRATION: {
      const {
        entity,
        changes,
        loading
      } = action as IntegrationActions.IntegrationUpdate;
      const existingIntegration = state.collection.find(
        item => item.id === entity.id
      );
      const updatedIntegration = {
        ...existingIntegration,
        ...entity,
        ...changes
      };
      const restCollection = state.collection.filter(
        item => item.id !== entity.id
      );

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
      const error = (action as IntegrationActions.IntegrationUpdateFail)
        .payload;
      const deletedIntegration = state.deleted;
      const restCollection = state.collection.filter(
        item => item.id !== state.inserted.id
      );

      return {
        ...state,
        collection: [deletedIntegration, ...restCollection],
        loading: false,
        hasErrors: true,
        errors: [error]
      };
    }

    case IntegrationActions.CREATE_INTEGRATION: {
      const {
        entity,
        loading
      } = action as IntegrationActions.IntegrationCreate;

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
      const error = (action as IntegrationActions.IntegrationUpdateFail)
        .payload;
      const restCollection = state.collection.filter(
        item => item.id !== state.inserted.id
      );

      return {
        ...state,
        collection: restCollection,
        loading: false,
        hasErrors: true,
        errors: [error]
      };
    }

    case IntegrationActions.DELETE_INTEGRATION: {
      const {
        entity,
        loading
      } = action as IntegrationActions.IntegrationDelete;
      const restCollection = state.collection.filter(
        item => item.id !== entity.id
      );
      const deletedIntegration = state.collection.find(
        item => item.id === entity.id
      );

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
      const error = (action as IntegrationActions.IntegrationDeleteFail)
        .payload;

      return {
        ...state,
        collection: [state.deleted, ...state.collection],
        loading: false,
        hasErrors: true,
        errors: [error]
      };
    }

    case IntegrationActions.FETCH_METRICS: {
      const id = (action as IntegrationActions.FetchMetrics).id;
      const list = state.metrics.list;
      if (id) {
        if (!list.some(integrationMetrics => integrationMetrics.id === id)) {
          list.push({ id, ...initialIntegrationMetrics });
        }
      }

      return {
        ...state,
        metrics: { ...state.metrics, list },
        loading: true,
        loaded: false,
        hasErrors: false,
        errors: []
      };
    }

    case IntegrationActions.FETCH_METRICS_COMPLETE: {
      const payload = (action as IntegrationActions.FetchMetricsComplete)
        .payload;
      let { list, summary } = state.metrics;
      if (payload.id) {
        if (
          list.some(integrationMetrics => integrationMetrics.id === payload.id)
        ) {
          list = list.filter(
            integrationMetrics => integrationMetrics.id !== payload.id
          );
        }

        list.push(payload);
      } else {
        summary = payload;
      }

      return {
        ...state,
        metrics: { ...state.metrics, summary, list },
        loading: false,
        loaded: true,
        hasErrors: false,
        errors: []
      };
    }

    default: {
      return state;
    }
  }
}

export const selectIntegrationState = createFeatureSelector<IntegrationState>(
  'integrationState'
);
