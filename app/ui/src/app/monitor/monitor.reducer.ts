import { ActionReducerMap, createFeatureSelector } from '@ngrx/store';

import { PlatformState } from '@syndesis/ui/platform';
import { MonitorState } from './monitor.models';
import * as MonitorActions from './monitor.actions';

const initialState: MonitorState = {
  metrics: {},
  logs: [],
  loading: false,
  loaded: false,
  hasErrors: false,
  errors: []
};

export function monitorReducer(state = initialState, action: any): MonitorState {
  switch (action.type) {

    case MonitorActions.FETCH_METRICS: {
      return {
        ...state,
        loading: true,
        loaded: false,
        hasErrors: false,
        errors: []
      };
    }

    case MonitorActions.FETCH_METRICS_COMPLETE: {
      const metrics = (action as MonitorActions.MetricsFetchComplete).payload;
      return {
        ...state,
        ...{ metrics },
        loading: false,
        loaded: true
      };
    }

    case MonitorActions.FETCH_METRICS_FAIL: {
      const error = (action as MonitorActions.MetricsFetchFail).payload;
      return {
        ...state,
        loading: false,
        loaded: true,
        hasErrors: true,
        errors: [error]
      };
    }

    case MonitorActions.FETCH_LOGS: {
      return {
        ...state,
        loading: true,
        loaded: false,
        hasErrors: false,
        errors: []
      };
    }

    case MonitorActions.FETCH_LOGS_COMPLETE: {
      const logs = (action as MonitorActions.LogsFetchComplete).payload;
      return {
        ...state,
        ...{ logs },
        loading: false,
        loaded: true
      };
    }

    case MonitorActions.FETCH_LOGS_FAIL: {
      const error = (action as MonitorActions.LogsFetchFail).payload;
      return {
        ...state,
        loading: false,
        loaded: true,
        hasErrors: true,
        errors: [error]
      };
    }

    // Please note that FETCH_LOG_DELTAS is meant to occur in the background
    // so it has no impact on the state.

    case MonitorActions.FETCH_LOG_DELTAS_COMPLETE: {
      const deltas = (action as MonitorActions.LogDeltasFetchComplete).logDeltas;
      const logs = [...state.logs, ...deltas];
      return {
        ...state,
        ...{ logs },
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

export interface MonitorStore extends PlatformState {
  monitorState: MonitorState;
}

export const getMonitorState = createFeatureSelector<MonitorState>('monitorState');
