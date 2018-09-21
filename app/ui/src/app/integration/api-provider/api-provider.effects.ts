import { /*filter,*/ mergeMap, map, /*switchMap,*/ catchError } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Observable, of } from 'rxjs';

// import { EventsService } from '@syndesis/ui/store';
import { ApiProviderService } from '@syndesis/ui/integration/api-provider/api-provider.service';
import {
  ApiProviderActions,
  ApiProviderValidateSwagger
} from '@syndesis/ui/integration/api-provider/api-provider.actions';

@Injectable()
export class ApiProviderEffects {

  @Effect()
  validateSwagger$: Observable<Action> = this.actions$
    .ofType<ApiProviderValidateSwagger>(ApiProviderActions.VALIDATE_SWAGGER)
    .pipe(
      mergeMap(action =>
        this.apiProviderService
          .validateOpenApiSpecification(action.payload)
          .pipe(
            map(response => ({
              type: ApiProviderActions.VALIDATE_SWAGGER_COMPLETE,
              payload: response
            })),
            catchError(error =>
              of({
                type: ApiProviderActions.VALIDATE_SWAGGER_FAIL,
                payload: error
              })
            )
          )
      )
    );

  constructor(
    private actions$: Actions,
    private apiProviderService: ApiProviderService,
  ) {}
}
