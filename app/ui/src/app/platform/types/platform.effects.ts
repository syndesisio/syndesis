import { map } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Observable } from 'rxjs';

import * as PlatformActions from './platform.actions';
import { IntegrationEffects, IntegrationActions } from './integration';
import { I18NEffects } from './i18n';

@Injectable()
export class PlatformEffects {
  @Effect()
  bootstrapIntegration$: Observable<Action> = this.actions$
    .ofType(PlatformActions.APP_BOOTSTRAP)
    .pipe(map(() => ({ type: IntegrationActions.FETCH_INTEGRATIONS })));

  constructor(private actions$: Actions) {}

  static rootEffects(): Array<any> {
    return [
      PlatformEffects,
      IntegrationEffects,
      I18NEffects
      // Add any new @Effects-decorated type below...
    ];
  }
}
