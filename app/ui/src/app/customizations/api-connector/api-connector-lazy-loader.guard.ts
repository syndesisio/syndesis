import { Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';
import { Observable, of } from 'rxjs';
import { first, switchMap } from 'rxjs/operators';
import { Store } from '@ngrx/store';

import { ApiConnectorState } from '@syndesis/ui/customizations/api-connector/api-connector.models';
import { ApiConnectorActions } from '@syndesis/ui/customizations/api-connector/api-connector.actions';
import {
  ApiConnectorStore,
  getApiConnectorState
} from '@syndesis/ui/customizations/api-connector/api-connector.reducer';

@Injectable()
export class ApiConnectorLazyLoaderGuard implements CanActivate {
  constructor(private apiConnectorStore: Store<ApiConnectorStore>) {}

  canActivate(): Observable<boolean> {
    return this.lazyLoadApiConnectors();
  }

  private lazyLoadApiConnectors(): Observable<boolean> {
    return this.apiConnectorStore
      .select<ApiConnectorState>(getApiConnectorState)
      .pipe(
        first(apiConnectorState => !apiConnectorState.loading),
        switchMap(apiConnectorState => {
          if (!apiConnectorState.loaded) {
            this.apiConnectorStore.dispatch(ApiConnectorActions.fetch());
          }
          return of(true);
        })
      );
  }
}
