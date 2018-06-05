import { map, mergeMap, catchError } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Observable, of } from 'rxjs';

import { IntegrationImportService } from './integration-import.service';
import * as IntegrationImportActions from './integration-import.actions';

@Injectable()
export class IntegrationImportEffects {
  @Effect()
  uploadIntegration$: Observable<Action> = this.actions$
    .ofType<IntegrationImportActions.IntegrationImportUpload>(
      IntegrationImportActions.UPLOAD_INTEGRATION
    )
    .pipe(
      mergeMap(action =>
        this.integrationImportService
          .uploadIntegration(action.entity)
          .pipe(
            map(response => ({
              type: IntegrationImportActions.UPLOAD_INTEGRATION_COMPLETE,
              payload: response
            })),
            catchError(error =>
              of({
                type: IntegrationImportActions.UPLOAD_INTEGRATION_FAIL,
                payload: error
              })
            )
          )
      )
    );

  constructor(
    private actions$: Actions,
    private integrationImportService: IntegrationImportService
  ) {}
}
