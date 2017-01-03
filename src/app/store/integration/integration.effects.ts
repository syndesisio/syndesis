import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/mergeMap';
import 'rxjs/add/operator/toArray';
import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Effect, Actions } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/startWith';

import { Integrations } from './integration.model';
import { IntegrationService } from './integration.service';
import { ActionTypes, LoadAction, LoadSuccessAction, LoadFailureAction } from './integration.actions';

@Injectable()
export class IntegrationEffects {

  @Effect()
  loadData$: Observable<Action> = this.actions$
    .ofType(ActionTypes.LOAD)
    .startWith(new LoadAction())
    .switchMap(() =>
      this.integrationService.list()
        .map((fetched: Integrations) => new LoadSuccessAction(fetched))
        .catch(error => Observable.of(new LoadFailureAction(error))),
  );

  constructor(private actions$: Actions, private integrationService: IntegrationService) { }

}
