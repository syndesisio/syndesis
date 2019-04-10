import {
  mergeMap,
  map,
  catchError,
  tap,
  withLatestFrom,
  filter,
} from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { Action, Store, select } from '@ngrx/store';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Observable, of } from 'rxjs';
import { ApiProviderService } from '@syndesis/ui/integration/api-provider/api-provider.service';
import {
  ApiProviderActions,
  ApiProviderCreate,
  ApiProviderCreateComplete,
  ApiProviderNextStep,
  ApiProviderPreviousStep,
  ApiProviderUpdateIntegrationName,
  ApiProviderUpdateSpecification,
  ApiProviderValidateSwagger,
} from '@syndesis/ui/integration/api-provider/api-provider.actions';
import {
  ApiProviderStore,
  getApiProviderIntegrationDescription,
  getApiProviderIntegrationName,
  getApiProviderSpecificationForEditor,
  getApiProviderSpecificationForValidation,
  getApiProviderWizardStep,
} from '@syndesis/ui/integration/api-provider/api-provider.reducer';
import { ApiProviderWizardSteps } from '@syndesis/ui/integration/api-provider/api-provider.models';
import { Router } from '@angular/router';
import {
  CurrentFlowService,
  FlowEvent,
  INTEGRATION_SET_PROPERTY,
} from '@syndesis/ui/integration';
import { Integration } from '@syndesis/ui/platform';

@Injectable()
export class ApiProviderEffects {
  @Effect()
  syncIntegrationNameFromService$ = this.currentFlowService.events
    .filter(
      (event: FlowEvent) =>
        event.kind === INTEGRATION_SET_PROPERTY && event.property === 'name'
    )
    .map((event: FlowEvent) => ({
      type: ApiProviderActions.UPDATE_INTEGRATION_NAME_FROM_SERVICE,
      payload: event.value,
    }));

  @Effect({ dispatch: false })
  syncIntegrationNameWithFromForm$ = this.actions$.pipe(
    ofType<ApiProviderUpdateIntegrationName>(
      ApiProviderActions.UPDATE_INTEGRATION_NAME
    ),
    tap(action => {
      this.currentFlowService.events.emit({
        kind: INTEGRATION_SET_PROPERTY,
        property: 'name',
        value: action.payload,
      });
    })
  );

  @Effect()
  reviewStep$: Observable<Action> = this.actions$.pipe(
    ofType<
      | ApiProviderPreviousStep
      | ApiProviderNextStep
      | ApiProviderUpdateSpecification
    >(
      ApiProviderActions.PREV_STEP,
      ApiProviderActions.NEXT_STEP,
      ApiProviderActions.UPDATE_SPEC
    ),
    withLatestFrom(
      this.apiProviderStore.pipe(select(getApiProviderWizardStep))
    ),
    filter(
      ([action, step]) => step === ApiProviderWizardSteps.ReviewApiProvider
    ),
    map(action => {
      return {
        type: ApiProviderActions.VALIDATE_SPEC,
      };
    })
  );

  @Effect()
  validateSwagger$: Observable<Action> = this.actions$.pipe(
    ofType<ApiProviderValidateSwagger>(ApiProviderActions.VALIDATE_SPEC),
    withLatestFrom(
      this.apiProviderStore.pipe(
        select(getApiProviderSpecificationForValidation)
      )
    ),
    mergeMap(([action, spec]) =>
      this.apiProviderService.validateOpenApiSpecification(spec).pipe(
        map(response => {
          if (response.actionsSummary) {
            return {
              type: ApiProviderActions.VALIDATE_SPEC_COMPLETE,
              payload: response,
            };
          }
          throw response;
        }),
        catchError(error =>
          of({
            type: ApiProviderActions.VALIDATE_SPEC_FAIL,
            payload: error,
          })
        )
      )
    )
  );

  @Effect()
  createIntegration$: Observable<Action> = this.actions$.pipe(
    ofType<ApiProviderCreate>(ApiProviderActions.CREATE),
    withLatestFrom(
      this.apiProviderStore.pipe(select(getApiProviderSpecificationForEditor))
    ),
    withLatestFrom(
      this.apiProviderStore.pipe(select(getApiProviderIntegrationName))
    ),
    withLatestFrom(
      this.apiProviderStore.pipe(select(getApiProviderIntegrationDescription))
    ),
    mergeMap(([[[action, spec], integrationName], integrationDescription]) =>
      this.apiProviderService.getIntegrationFromSpecification(spec).pipe(
        mergeMap((integrationFromSpec: Integration) => {
          integrationFromSpec.name = integrationName;
          integrationFromSpec.description = integrationDescription;
          return this.apiProviderService
            .createIntegration(integrationFromSpec)
            .pipe(
              map((newIntegration: Integration) => ({
                type: ApiProviderActions.CREATE_COMPLETE,
                payload: newIntegration,
              })),
              catchError(error =>
                of({
                  type: ApiProviderActions.CREATE_FAIL,
                  payload: error,
                })
              )
            );
        }),
        catchError(error =>
          of({
            type: ApiProviderActions.CREATE_FAIL,
            payload: error,
          })
        )
      )
    )
  );

  @Effect({ dispatch: false })
  integrationCreated$ = this.actions$.pipe(
    ofType<ApiProviderCreateComplete>(ApiProviderActions.CREATE_COMPLETE),
    tap((action: ApiProviderCreateComplete) =>
      this.router.navigate(['/integrations', action.payload.id, 'operations'])
    )
  );

  constructor(
    private actions$: Actions,
    private apiProviderService: ApiProviderService,
    private apiProviderStore: Store<ApiProviderStore>,
    private router: Router,
    private currentFlowService: CurrentFlowService
  ) {}
}
