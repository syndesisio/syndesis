import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';

import { EventsService } from '@syndesis/ui/store';
import { ApiConnectorService } from './api-connector.service';
import {
  ApiConnectorActions,
  ApiConnectorValidateSwagger,
  ApiConnectorCreate,
  ApiConnectorCreateComplete,
  ApiConnectorUpdate,
  ApiConnectorDelete,
  ApiConnectorDeleteComplete
} from './api-connector.actions';

@Injectable()
export class ApiConnectorEffects {
  @Effect()
  fetchApiConnectors$: Observable<Action> = this.actions$
    .ofType(
    ApiConnectorActions.FETCH,
    ApiConnectorActions.UPDATE_FAIL,
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
        .validateCustomConnectorInfo(action.payload)
        .map(response => ({ type: ApiConnectorActions.VALIDATE_SWAGGER_COMPLETE, payload: response }))
        .catch(error => Observable.of({
          type: ApiConnectorActions.VALIDATE_SWAGGER_FAIL,
          payload: error
        }))
    );

  @Effect()
  createCustomConnector$: Observable<Action> = this.actions$
    .ofType<ApiConnectorCreate>(ApiConnectorActions.CREATE)
    .mergeMap(action =>
      this.apiConnectorService
        .createCustomConnector(action.payload)
        .map(response => ({ type: ApiConnectorActions.CREATE_COMPLETE, payload: response }))
        .catch(error => Observable.of({
          type: ApiConnectorActions.CREATE_FAIL,
          payload: error
        }))
    );

  @Effect()
  updateCustomConnector$: Observable<Action> = this.actions$
    .ofType<ApiConnectorUpdate>(ApiConnectorActions.UPDATE)
    .mergeMap((action: ApiConnectorUpdate) => {
      const hasUpdatedIcon = !!action.payload.iconFile;
      return this.apiConnectorService
        .updateCustomConnector(action.payload)
        .map(response => ({ type: ApiConnectorActions.UPDATE_COMPLETE, payload: hasUpdatedIcon }))
        .catch(error => Observable.of({
          type: ApiConnectorActions.UPDATE_FAIL,
          payload: error
        }));
    });

  @Effect()
  refreshApiConnectors$: Observable<Action> = this.actions$
    .ofType(
      ApiConnectorActions.CREATE_COMPLETE,
      ApiConnectorActions.UPDATE_COMPLETE
    )
    .switchMap(() => Observable.of(ApiConnectorActions.fetch()));

  @Effect()
  deleteCustomConnector$: Observable<Action> = this.actions$
    .ofType<ApiConnectorDelete>(ApiConnectorActions.DELETE)
    .mergeMap(action =>
      this.apiConnectorService
        .deleteCustomConnector(action.payload)
        .map(response => ({ type: ApiConnectorActions.DELETE_COMPLETE, payload: response }))
        .catch(error => Observable.of({
          type: ApiConnectorActions.DELETE_FAIL,
          payload: error
        }))
  );
  
  @Effect()
  watchCustomConnectorUse$: Observable<Action> = this.actions$
    .ofType(ApiConnectorActions.FETCH_COMPLETE)
    .switchMap(() => {
      return this.eventsService.changeEvents
        .filter(event => event.kind === 'integration' && event.action === 'updated')
        .switchMap(() => Observable.of(ApiConnectorActions.fetch()));
    });

  constructor(
    private actions$: Actions,
    private apiConnectorService: ApiConnectorService,
    private eventsService: EventsService
  ) { }
}
