import { createFeatureSelector } from '@ngrx/store';

import { I18NState, I18N_DEFAULT_LOCALE } from '@syndesis/ui/platform/types/i18n/i18n.models';
import * as I18NActions from '@syndesis/ui/platform/types/i18n/i18n.actions';

const initialState: I18NState = {
  dictionary: null,
  locale: I18N_DEFAULT_LOCALE,
  onSync: true,
  onError: false
};

export function i18nReducer(state = initialState, action: any): I18NState {
  switch (action.type) {
    case I18NActions.FETCH: {
      return {
        ...state,
        onSync: true,
        onError: false
      };
    }

    case I18NActions.FETCH_COMPLETE: {
      return {
        ...state,
        ...(action as I18NActions.I18NFetchComplete).payload,
        onSync: false,
        onError: false
      };
    }

    case I18NActions.FETCH_FAIL: {
      return {
        ...state,
        onSync: false,
        onError: true
      };
    }

    case I18NActions.APPEND_APP_DETAILS: {
      const dictionary = {
        ...state.dictionary,
        ...(action as I18NActions.I18NAppendAppDetails).payload
      };

      return {
        ...state,
        dictionary,
        onSync: false,
        onError: false
      };
    }

    default: {
      return state;
    }
  }
}

export const selectI18NState = createFeatureSelector<I18NState>('i18nState');
