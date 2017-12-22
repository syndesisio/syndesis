import 'rxjs/add/operator/map';
import 'rxjs/add/operator/mergeMap';
import 'rxjs/add/operator/catch';
import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Action } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { of } from 'rxjs/observable/of';

import { ApiHttpService } from '@syndesis/ui/platform';
import { ApiConnectorActions, ApiConnectorValidateSwagger } from './api-connector.actions';

const ERROR_MSG = 'An unexpected HTTP error occured. Please check stack strace';

@Injectable()
export class ApiConnectorEffects {
  @Effect() validateSwagger$: Observable<Action> = this.actions$
    .ofType(ApiConnectorActions.VALIDATE_SWAGGER)
    .mergeMap((action: ApiConnectorValidateSwagger) =>
      this.apiHttpService.setEndpointUrl('submitCustomConnectorInfo')
        .post(action.payload)
        .map(response => ({ type: ApiConnectorActions.VALIDATE_SWAGGER_COMPLETE, payload: response }))
        .catch(error => of({ type: ApiConnectorActions.VALIDATE_SWAGGER_FAIL, payload: { message: ERROR_MSG } }))
    );

  constructor(
    private actions$: Actions,
    private apiHttpService: ApiHttpService
  ) { }
}
