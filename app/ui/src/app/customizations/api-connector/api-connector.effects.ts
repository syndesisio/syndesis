import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';

import { ApiConnectorService } from './api-connector.service';
import {
  ApiConnectorActions,
  ApiConnectorValidateSwagger,
  ApiConnectorCreate,
  ApiConnectorCreateComplete,
  ApiConnectorDelete,
  ApiConnectorDeleteComplete
} from './api-connector.actions';

@Injectable()
export class ApiConnectorEffects {
  @Effect()
  fetchApiConnectors$: Observable<Action> = this.actions$
    .ofType(
      ApiConnectorActions.FETCH,
      ApiConnectorActions.DELETE_FAIL
    )
    .mergeMap(() =>
      this.apiConnectorService
        .getCustomConnectorList()
        .map(response => ({ type: ApiConnectorActions.FETCH_COMPLETE, payload: response }))
        .catch(error => Observable.of({
          type: ApiConnectorActions.FETCH_FAIL,
          payload: error
        }))
  );

  @Effect()
  validateSwagger$: Observable<Action> = this.actions$
    .ofType<ApiConnectorValidateSwagger>(ApiConnectorActions.VALIDATE_SWAGGER)
    .mergeMap(action =>
      this.apiConnectorService
        .submitCustomConnectorInfo(action.payload)
        .map(response => ({ type: ApiConnectorActions.VALIDATE_SWAGGER_COMPLETE, payload: response }))
        .catch(error => Observable.of({
          type: ApiConnectorActions.VALIDATE_SWAGGER_FAIL,
          payload: error
        }))
    );

  @Effect()
  createCustomConnector$: Observable<Action> = this.actions$
    .ofType<ApiConnectorCreate>(ApiConnectorActions.CREATE)
    .mergeMap(action  =>
      this.apiConnectorService
        .createCustomConnector(action.payload)
        .map(response => ({ type: ApiConnectorActions.CREATE_COMPLETE, payload: response }))
        .catch(error => Observable.of({
          type: ApiConnectorActions.CREATE_FAIL,
          payload: error
        }))
    );

  @Effect()
  refreshApiConnectors$: Observable<Action> = this.actions$
    .ofType<ApiConnectorCreateComplete>(ApiConnectorActions.CREATE_COMPLETE)
    .switchMap(() => Observable.of(ApiConnectorActions.fetch()));

  @Effect()
  deleteCustomConnector$: Observable<Action> = this.actions$
    .ofType<ApiConnectorDelete>(ApiConnectorActions.DELETE)
    .mergeMap(action  =>
      this.apiConnectorService
        .deleteCustomConnector(action.payload)
        .map(response => ({ type: ApiConnectorActions.DELETE_COMPLETE, payload: response }))
        .catch(error => Observable.of({
          type: ApiConnectorActions.DELETE_FAIL,
          payload: error
        }))
    );

  constructor(
    private actions$: Actions,
    private apiConnectorService: ApiConnectorService
  ) { }
}
