import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';

import { IntegrationImportService } from './integration-import.service';
import * as IntegrationImportActions from './integration-import.actions';

@Injectable()
export class IntegrationImportEffects {
  @Effect()
  uploadIntegration$: Observable<Action> = this.actions$
    .ofType<IntegrationImportActions.IntegrationImportUpload>(IntegrationImportActions.UPLOAD_INTEGRATION)
    .mergeMap(action =>
      this.integrationImportService
        .uploadIntegration(action.entity)
        .map(response => ({ type: IntegrationImportActions.UPLOAD_INTEGRATION_COMPLETE, payload: response }))
        .catch(error => Observable.of({
          type: IntegrationImportActions.UPLOAD_INTEGRATION_FAIL,
          payload: error
        }))
    );

  constructor(
    private actions$: Actions,
    private integrationImportService: IntegrationImportService
  ) { }
}
