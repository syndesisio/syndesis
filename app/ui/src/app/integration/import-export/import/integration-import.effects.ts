import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';

import { IntegrationImportService } from './integration-import.service';
import {
  IntegrationImportActions,
  IntegrationUpload,
  IntegrationUploadComplete,
  IntegrationImport,
  IntegrationImportComplete
} from './integration-import.actions';

@Injectable()
export class IntegrationImportEffects {
  @Effect()
  importIntegration$: Observable<Action> = this.actions$
    .ofType<IntegrationUpload>(IntegrationImportActions.IMPORT)
    .mergeMap(action =>
      this.importService
        .importIntegration(action.payload)
        .map(response => ({ type: IntegrationImportActions.IMPORT_COMPLETE, payload: response }))
        .catch(error => Observable.of({
          type: IntegrationImportActions.IMPORT_FAIL,
          payload: error
        }))
    );

  constructor(
    private actions$: Actions,
    private importService: IntegrationImportService
  ) { }
}
