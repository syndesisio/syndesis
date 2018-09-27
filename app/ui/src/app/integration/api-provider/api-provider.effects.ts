import { mergeMap, map, catchError, tap } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { Action, Store } from '@ngrx/store';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Observable, of } from 'rxjs';
import { ApiProviderService } from '@syndesis/ui/integration/api-provider/api-provider.service';
import {
  ApiProviderActions, ApiProviderCreate, ApiProviderCreateComplete,
  ApiProviderNextStep,
  ApiProviderPreviousStep,
  ApiProviderUpdateSpecification,
  ApiProviderValidateSwagger
} from '@syndesis/ui/integration/api-provider/api-provider.actions';
import {
  ApiProviderStore,
  getApiProviderIntegrationName,
  getApiProviderSpecificationForEditor,
  getApiProviderSpecificationForValidation,
  getApiProviderWizardStep
} from '@syndesis/ui/integration/api-provider/api-provider.reducer';
import { ApiProviderWizardSteps } from '@syndesis/ui/integration/api-provider/api-provider.models';
import { Router } from '@angular/router';
import { CurrentFlowService, FlowEvent } from '@syndesis/ui/integration';
import { Integration } from '@syndesis/ui/platform';

@Injectable()
export class ApiProviderEffects {

  @Effect()
  currentFlow$ = this.currentFlowService.events
    .filter((event: FlowEvent) => event.kind === 'integration-set-property' && event.property === 'name')
    .map((event: FlowEvent) => ({
      type: ApiProviderActions.UPDATE_SPEC_TITLE,
      payload: event.value
    }));

  @Effect()
  reviewStep$: Observable<Action> = this.actions$
    .ofType<ApiProviderPreviousStep | ApiProviderNextStep | ApiProviderUpdateSpecification>(
      ApiProviderActions.PREV_STEP,
      ApiProviderActions.NEXT_STEP,
      ApiProviderActions.UPDATE_SPEC
    )
    .withLatestFrom(this.apiProviderStore.select(
      getApiProviderWizardStep
    ))
    .filter(([action, step]) => step === ApiProviderWizardSteps.ReviewApiProvider)
    .map(action => {
      return {
        type: ApiProviderActions.VALIDATE_SPEC
      };
    });

  @Effect()
  validateSwagger$: Observable<Action> = this.actions$
    .ofType<ApiProviderValidateSwagger>(ApiProviderActions.VALIDATE_SPEC)
    .withLatestFrom(this.apiProviderStore.select(
      getApiProviderSpecificationForValidation
    ))
    .pipe(
      mergeMap(([action, spec]) =>
        this.apiProviderService
          .validateOpenApiSpecification(spec)
          .pipe(
            map(response => {
              if (response.actionsSummary) {
                return {
                  type: ApiProviderActions.VALIDATE_SPEC_COMPLETE,
                  payload: response
                };
              }
              throw response;
            }),
            catchError(error =>
              of({
                type: ApiProviderActions.VALIDATE_SPEC_FAIL,
                payload: error
              })
            )
          )
      )
    );

  @Effect()
  createIntegration$: Observable<Action> = this.actions$
    .ofType<ApiProviderCreate>(ApiProviderActions.CREATE)
    .withLatestFrom(this.apiProviderStore.select(
      getApiProviderSpecificationForEditor
    ))
    .withLatestFrom(this.apiProviderStore.select(
      getApiProviderIntegrationName
    ))
    .pipe(
      mergeMap(([[action, spec], integrationName]) =>
        this.apiProviderService
          .getIntegrationFromSpecification(spec)
          .pipe(
            mergeMap((integrationFromSpec: Integration) => {
              integrationFromSpec.name = integrationName || integrationFromSpec.name;
              return this.apiProviderService
                .createIntegration(integrationFromSpec)
                .pipe(
                  map((newIntegration: Integration) => ({
                    type: ApiProviderActions.CREATE_COMPLETE,
                    payload: newIntegration
                  })),
                  catchError(error =>
                    of({
                      type: ApiProviderActions.CREATE_FAIL,
                      payload: error
                    })
                  )
                );
            }),
            catchError(error =>
              of({
                type: ApiProviderActions.CREATE_FAIL,
                payload: error
              })
            )
          )
      )
    );

  @Effect({ dispatch: false })
  integrationCreated$ = this.actions$
    .pipe(
      ofType<ApiProviderCreateComplete>(ApiProviderActions.CREATE_COMPLETE),
      tap((action: ApiProviderCreateComplete) => this.router.navigate([
        '/integrations', action.payload.id , 'operations'
      ]))
    );

  constructor(
    private actions$: Actions,
    private apiProviderService: ApiProviderService,
    private apiProviderStore: Store<ApiProviderStore>,
    private router: Router,
    private currentFlowService: CurrentFlowService
  ) {}
}
