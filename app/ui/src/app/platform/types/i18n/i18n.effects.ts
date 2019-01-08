import { of as observableOf, Observable } from 'rxjs';

import { map, switchMap, catchError, tap } from 'rxjs/operators';
import { PlatformActions } from '@syndesis/ui/platform';
import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Actions, Effect, ofType } from '@ngrx/effects';

import { moment } from '@syndesis/ui/vendor';

import * as I18NActions from '@syndesis/ui/platform/types/i18n/i18n.actions';
import { I18NService } from '@syndesis/ui/platform/types/i18n/i18n.service';
import { I18NState, I18N_DEFAULT_LOCALE } from '@syndesis/ui/platform/types/i18n/i18n.models';

// TODO: is this ok? https://github.com/ngrx/platform/issues/31
interface PayloadAction extends Action {
  type: string;
  payload?: any;
}

@Injectable()
export class I18NEffects {
  @Effect()
  fetch$: Observable<Action> = this.actions$
    .pipe(
      ofType<PayloadAction>(I18NActions.FETCH),
      switchMap(action => this.i18nService.setLocale(action.payload)),
      map((response: I18NState) => new I18NActions.I18NFetchComplete(response)),
      catchError(() => observableOf(new I18NActions.I18NFetchFail()))
    );

  @Effect({ dispatch: false })
  updateLocale$: Observable<PayloadAction> = this.actions$
    .pipe(
      ofType<PayloadAction>(I18NActions.FETCH_COMPLETE),
      tap(action => {
        const { locale } = action.payload;
        this.i18nService.persistLocale(locale);
        moment.locale(locale);

        // const localePath = `@angular/common/locales/${locale}.js`;
        // TODO: Fix Angular's locale registration and raised warnings
        //import(localePath).then(locale => registerLocaleData(locale.default));
      })
    );

  @Effect()
  localize$: Observable<Action> = this.actions$
    .pipe(
      ofType(PlatformActions.APP_BOOTSTRAP),
      map(
        action =>
          new I18NActions.I18NFetch(
            this.i18nService.getLocale(I18N_DEFAULT_LOCALE)
          )
      )
    );

  constructor(private actions$: Actions, private i18nService: I18NService) {}
}
