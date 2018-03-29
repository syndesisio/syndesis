import { PlatformActions } from '@syndesis/ui/platform';
import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';

import { moment } from '@syndesis/ui/vendor';

import * as I18NActions from './i18n.actions';
import { I18NService } from './i18n.service';
import { I18NState, I18N_DEFAULT_LOCALE } from './i18n.models';
import { registerLocaleData } from '@angular/common';

@Injectable()
export class I18NEffects {
  constructor(
    private actions$: Actions,
    private i18nService: I18NService
  ) { }

  @Effect() fetch$: Observable<Action> = this.actions$
    .ofType<I18NActions.I18NFetch>(I18NActions.FETCH)
    .switchMap(action => this.i18nService.setLocale(action.payload))
    .map((response: I18NState) => new I18NActions.I18NFetchComplete(response))
    .catch(() => Observable.of(new I18NActions.I18NFetchFail()));

  @Effect({ dispatch: false }) updateLocale$: Observable<Action> = this.actions$
    .ofType<I18NActions.I18NFetchComplete>(I18NActions.FETCH_COMPLETE)
    .do(action => {
      const { locale } = action.payload;
      this.i18nService.persistLocale(locale);
      moment.locale(locale);

      const localePath = `@angular/common/locales/${locale}.js`;
      // TODO: Fix Angular's locale registration and raised warnings
      //import(localePath).then(locale => registerLocaleData(locale.default));
    });

  @Effect() localize$: Observable<Action> = this.actions$
    .ofType(PlatformActions.APP_BOOTSTRAP)
    .map(action => new I18NActions.I18NFetch(this.i18nService.getLocale(I18N_DEFAULT_LOCALE)));
}