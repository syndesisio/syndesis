import { Action } from '@ngrx/store';

import { I18NState, DictionaryEntry, I18N_DEFAULT_LOCALE } from '@syndesis/ui/platform/types/i18n/i18n.models';

export const FETCH = '[i18n] Fetch generic dictionary request';
export const FETCH_COMPLETE = '[i18n] Fetch generic dictionary complete';
export const FETCH_FAIL = '[i18n] Fetch generic dictionary failed';
export const APPEND_APP_DETAILS = '[i18n] Application details updated';

/**
 * Statically typed action classes, with constructors
 * exposing typed payload where required.
 */
export class I18NFetch implements Action {
  readonly type = FETCH;

  constructor(public payload: string = I18N_DEFAULT_LOCALE) {}
}

export class I18NFetchComplete implements Action {
  readonly type = FETCH_COMPLETE;

  constructor(public payload: I18NState) {}
}

export class I18NFetchFail implements Action {
  readonly type = FETCH_FAIL;
}

export class I18NAppendAppDetails implements Action {
  readonly type = APPEND_APP_DETAILS;

  constructor(public payload: DictionaryEntry) {}
}
