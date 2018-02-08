import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';

import * as PlatformActions from './platform.actions';
import { IntegrationEffects, IntegrationActions } from './integration';

@Injectable()
export class PlatformEffects {
  @Effect()
  bootstrapIntegration$: Observable<Action> = this.actions$
    .ofType(PlatformActions.APP_BOOTSTRAP)
    .map(() => ({ type: IntegrationActions.FETCH_INTEGRATIONS }));

  constructor(private actions$: Actions) { }

  static rootEffects(): Array<any> {
    return [
      PlatformEffects,
      IntegrationEffects,
      // Add any new @Effects-decorated type below...
    ];
  }
}
