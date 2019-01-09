import { map } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Observable } from 'rxjs';

import * as PlatformActions from '@syndesis/ui/platform/types/platform.actions';
import { IntegrationEffects, IntegrationActions } from '@syndesis/ui/platform/types/integration';
import { I18NEffects } from '@syndesis/ui/platform/types/i18n';

@Injectable()
export class PlatformEffects {
  @Effect()
  bootstrapIntegration$: Observable<Action> = this.actions$
    .pipe(
      ofType(PlatformActions.APP_BOOTSTRAP),
      map(() => ({ type: IntegrationActions.FETCH_INTEGRATIONS }))
    );

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
