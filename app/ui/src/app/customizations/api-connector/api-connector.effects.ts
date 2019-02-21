import { filter, mergeMap, map, switchMap, catchError, tap, withLatestFrom } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { Action, Store, select } from '@ngrx/store';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Observable, of } from 'rxjs';

import { EventsService } from '@syndesis/ui/store';
import { ApiConnectorService } from '@syndesis/ui/customizations/api-connector/api-connector.service';
import {
  ApiConnectorActions,
  ApiConnectorValidateSwagger,
  ApiConnectorCreate,
  ApiConnectorUpdate,
  ApiConnectorDelete,
  ApiConnectorPreviousStep,
  ApiConnectorNextStep,
  ApiConnectorUpdateSpecification,
  ApiConnectorCreateComplete,
  ApiConnectorSetData
} from '@syndesis/ui/customizations/api-connector/api-connector.actions';
import {
  ApiConnectorStore, getApiConnectorSpecificationForValidation,
  getApiConnectorWizardStep
} from '@syndesis/ui/customizations/api-connector/api-connector.reducer';
import { ApiConnectorWizardStep } from '@syndesis/ui/customizations/api-connector/api-connector.models';
import { Router } from '@angular/router';

@Injectable()
export class ApiConnectorEffects {
  @Effect()
  fetchApiConnectors$: Observable<Action> = this.actions$
    .pipe(
      ofType(
        ApiConnectorActions.FETCH,
        ApiConnectorActions.UPDATE_FAIL,
        ApiConnectorActions.DELETE_FAIL,
        ApiConnectorActions.CREATE_CANCEL
      ),
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
  reviewStep$: Observable<Action> = this.actions$
    .pipe(
      ofType<ApiConnectorPreviousStep | ApiConnectorNextStep | ApiConnectorUpdateSpecification>(
        ApiConnectorActions.PREV_STEP,
        ApiConnectorActions.NEXT_STEP,
        ApiConnectorActions.UPDATE_SPEC
      ),
      withLatestFrom(this.apiConnectorStore.pipe(select(
        getApiConnectorWizardStep
      ))),
      filter(([action, step]) => step === ApiConnectorWizardStep.ReviewApiConnector),
      map(action => {
        return {
          type: ApiConnectorActions.VALIDATE_SWAGGER
        };
      })
    );

  @Effect()
  validateSwagger$: Observable<Action> = this.actions$
    .pipe(
      ofType<ApiConnectorValidateSwagger>(ApiConnectorActions.VALIDATE_SWAGGER),
      withLatestFrom(this.apiConnectorStore.pipe(select(
        getApiConnectorSpecificationForValidation
      ))),
      mergeMap(([action, request]) =>
        this.apiConnectorService
          .validateCustomConnectorInfo(request)
          .pipe(
            map(response => ({
              type: response.errors ? ApiConnectorActions.VALIDATE_SWAGGER_FAIL : ApiConnectorActions.VALIDATE_SWAGGER_COMPLETE,
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
    .pipe(
      ofType<ApiConnectorCreate>(ApiConnectorActions.CREATE),
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
    .pipe(
      ofType<ApiConnectorUpdate>(ApiConnectorActions.UPDATE),
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
    .pipe(
      ofType(
        ApiConnectorActions.CREATE_COMPLETE,
        ApiConnectorActions.UPDATE_COMPLETE
      ),
      switchMap(() => of(ApiConnectorActions.fetch()))
    );

  @Effect()
  deleteCustomConnector$: Observable<Action> = this.actions$
    .pipe(
      ofType<ApiConnectorDelete>(ApiConnectorActions.DELETE),
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
    .pipe(
      ofType(ApiConnectorActions.FETCH_COMPLETE),
      switchMap(() => {
        return this.eventsService.changeEvents.pipe(
          filter(
            event => event.kind === 'integration' && event.action === 'updated'
          ),
          switchMap(() => of(ApiConnectorActions.fetch()))
        );
      })
    );

  @Effect({ dispatch: false })
  connectorCreated$ = this.actions$
    .pipe(
      ofType<ApiConnectorCreateComplete>(ApiConnectorActions.CREATE_COMPLETE),
      tap((action: ApiConnectorCreateComplete) => this.router.navigate([
        '/customizations', 'api-connector'
      ]))
    );

  @Effect({ dispatch: false })
  showConnectorDetail$ = this.actions$
    .pipe(
      ofType<ApiConnectorSetData>(ApiConnectorActions.SET_CONNECTOR_DATA),
      tap((action: ApiConnectorSetData) =>
        this.router.navigate([action.payload.id], { relativeTo: action.route })
      )
    );

  constructor(
    private actions$: Actions,
    private apiConnectorService: ApiConnectorService,
    private apiConnectorStore: Store<ApiConnectorStore>,
    private router: Router,
    private eventsService: EventsService
  ) {}
}
