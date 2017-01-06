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

import { Connections } from './connection.model';
import { ConnectionService } from './connection.service';
import { ActionTypes, LoadAction, LoadSuccessAction, LoadFailureAction } from './connection.actions';

@Injectable()
export class ConnectionEffects {

  @Effect()
  loadData: Observable<Action> = this.actions
    .ofType(ActionTypes.LOAD)
    .startWith(new LoadAction())
    .switchMap(() =>
      this.connectionService.list()
        .map((fetched: Connections) => new LoadSuccessAction(fetched))
        .catch(error => Observable.of(new LoadFailureAction(error))),
  );

  constructor(private actions: Actions, private connectionService: ConnectionService) { }

}
