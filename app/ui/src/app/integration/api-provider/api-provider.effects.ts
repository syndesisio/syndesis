import { mergeMap, map, catchError } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { Action, Store } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Observable, of } from 'rxjs';
import { ApiProviderService } from '@syndesis/ui/integration/api-provider/api-provider.service';
import {
  ApiProviderActions,
  ApiProviderNextStep,
  ApiProviderPreviousStep,
  ApiProviderUpdateSpecification,
  ApiProviderValidateSwagger
} from '@syndesis/ui/integration/api-provider/api-provider.actions';
import {
  ApiProviderStore,
  getApiProviderSpecificationForValidation,
  getApiProviderWizardStep
} from '@syndesis/ui/integration/api-provider/api-provider.reducer';
import { ApiProviderWizardSteps } from '@syndesis/ui/integration/api-provider/api-provider.models';

@Injectable()
export class ApiProviderEffects {

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

  constructor(
    private actions$: Actions,
    private apiProviderService: ApiProviderService,
    private apiProviderStore: Store<ApiProviderStore>
  ) {}
}
