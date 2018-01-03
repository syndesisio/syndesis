import { Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';
import { Observable } from 'rxjs/Rx';
import { Store } from '@ngrx/store';

import { ApiConnectorState } from './api-connector.models';
import { ApiConnectorActions } from './api-connector.actions';
import { ApiConnectorStore, getApiConnectorState } from './api-connector.reducer';

@Injectable()
export class ApiConnectorLazyLoaderGuard implements CanActivate {
  constructor(private apiConnectorStore: Store<ApiConnectorStore>) { }

  canActivate(): Observable<boolean> {
    return this.lazyLoadApiConnectors();
  }

  private lazyLoadApiConnectors(): Observable<boolean> {
    return this.apiConnectorStore.select<ApiConnectorState>(getApiConnectorState)
      .first(apiConnectorState => !apiConnectorState.loading)
      .switchMap(apiConnectorState => {
        if (!apiConnectorState.loaded) {
          this.apiConnectorStore.dispatch(ApiConnectorActions.fetch());
        }

        return Observable.of(true);
      });
  }
}
