import 'rxjs/add/operator/map';
import 'rxjs/add/operator/mergeMap';
import 'rxjs/add/operator/catch';
import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Action } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { of } from 'rxjs/observable/of';

import { ApiConnectorService } from './api-connector.service';
import {
  ApiConnectorActions,
  ApiConnectorValidateSwagger,
  ApiConnectorCreate
} from './api-connector.actions';

@Injectable()
export class ApiConnectorEffects {
  @Effect()
  validateSwagger$: Observable<Action> = this.actions$
    .ofType(ApiConnectorActions.VALIDATE_SWAGGER)
    .mergeMap((action: ApiConnectorValidateSwagger) =>
      this.apiConnectorService
        .submitCustomConnectorInfo(action.payload)
        .map(response => ({ type: ApiConnectorActions.VALIDATE_SWAGGER_COMPLETE, payload: response }))
        .catch(error => of({
          type: ApiConnectorActions.VALIDATE_SWAGGER_FAIL,
          payload: error
        }))
    );

  @Effect()
  createCustomConnector$: Observable<Action> = this.actions$
    .ofType(ApiConnectorActions.CREATE)
    .mergeMap((action: ApiConnectorCreate) =>
      this.apiConnectorService
        .createCustomConnector(action.payload)
        .map(response => ({ type: ApiConnectorActions.CREATE_COMPLETE, payload: response }))
        .catch(error => of({
          type: ApiConnectorActions.CREATE_FAIL,
          payload: error
        }))
    );

  constructor(
    private actions$: Actions,
    private apiConnectorService: ApiConnectorService
  ) { }
}
