import { filter, mergeMap, map, switchMap, catchError } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Observable, of } from 'rxjs';

import { EventsService } from '@syndesis/ui/store';
import { ApiConnectorService } from '@syndesis/ui/customizations/api-connector/api-connector.service';
import {
  ApiConnectorActions,
  ApiConnectorValidateSwagger,
  ApiConnectorCreate,
  ApiConnectorUpdate,
  ApiConnectorDelete
} from '@syndesis/ui/customizations/api-connector/api-connector.actions';

@Injectable()
export class ApiConnectorEffects {
  @Effect()
  fetchApiConnectors$: Observable<Action> = this.actions$
    .ofType(
      ApiConnectorActions.FETCH,
      ApiConnectorActions.UPDATE_FAIL,
      ApiConnectorActions.DELETE_FAIL
    )
    .pipe(
      mergeMap(() =>
        this.apiConnectorService
          .getCustomConnectorList()
          .pipe(
            map(response => ({
              type: ApiConnectorActions.FETCH_COMPLETE,
              payload: response
            })),
            catchError(error =>
              of({
                type: ApiConnectorActions.FETCH_FAIL,
                payload: error
              })
            )
          )
      )
    );

  @Effect()
  validateSwagger$: Observable<Action> = this.actions$
    .ofType<ApiConnectorValidateSwagger>(ApiConnectorActions.VALIDATE_SWAGGER)
    .pipe(
      mergeMap(action =>
        this.apiConnectorService
          .validateCustomConnectorInfo(action.payload)
          .pipe(
            map(response => ({
              type: ApiConnectorActions.VALIDATE_SWAGGER_COMPLETE,
              payload: response
            })),
            catchError(error =>
              of({
                type: ApiConnectorActions.VALIDATE_SWAGGER_FAIL,
                payload: error
              })
            )
          )
      )
    );

  @Effect()
  createCustomConnector$: Observable<Action> = this.actions$
    .ofType<ApiConnectorCreate>(ApiConnectorActions.CREATE)
    .pipe(
      mergeMap(action =>
        this.apiConnectorService
          .createCustomConnector(action.payload)
          .pipe(
            map(response => ({
              type: ApiConnectorActions.CREATE_COMPLETE,
              payload: response
            })),
            catchError(error =>
              of({
                type: ApiConnectorActions.CREATE_FAIL,
                payload: error
              })
            )
          )
      )
    );

  @Effect()
  updateCustomConnector$: Observable<Action> = this.actions$
    .ofType<ApiConnectorUpdate>(ApiConnectorActions.UPDATE)
    .pipe(
      mergeMap((action: ApiConnectorUpdate) => {
        const hasUpdatedIcon = !!action.payload.iconFile;
        return this.apiConnectorService
          .updateCustomConnector(action.payload)
          .pipe(
            map(response => ({
              type: ApiConnectorActions.UPDATE_COMPLETE,
              payload: hasUpdatedIcon
            })),
            catchError(error =>
              of({
                type: ApiConnectorActions.UPDATE_FAIL,
                payload: error
              })
            )
          );
      })
    );

  @Effect()
  refreshApiConnectors$: Observable<Action> = this.actions$
    .ofType(
      ApiConnectorActions.CREATE_COMPLETE,
      ApiConnectorActions.UPDATE_COMPLETE
    )
    .pipe(switchMap(() => of(ApiConnectorActions.fetch())));

  @Effect()
  deleteCustomConnector$: Observable<Action> = this.actions$
    .ofType<ApiConnectorDelete>(ApiConnectorActions.DELETE)
    .pipe(
      mergeMap(action =>
        this.apiConnectorService
          .deleteCustomConnector(action.payload)
          .pipe(
            map(response => ({
              type: ApiConnectorActions.DELETE_COMPLETE,
              payload: response
            })),
            catchError(error =>
              of({
                type: ApiConnectorActions.DELETE_FAIL,
                payload: error
              })
            )
          )
      )
    );

  @Effect()
  watchCustomConnectorUse$: Observable<Action> = this.actions$
    .ofType(ApiConnectorActions.FETCH_COMPLETE)
    .pipe(
      switchMap(() => {
        return this.eventsService.changeEvents.pipe(
          filter(
            event => event.kind === 'integration' && event.action === 'updated'
          ),
          switchMap(() => of(ApiConnectorActions.fetch()))
        );
      })
    );

  constructor(
    private actions$: Actions,
    private apiConnectorService: ApiConnectorService,
    private eventsService: EventsService
  ) {}
}
