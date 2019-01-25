import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  Resolve,
  RouterStateSnapshot
} from '@angular/router';
import { combineLatest, Observable, of } from 'rxjs';
import { map, take, filter, switchMap } from 'rxjs/operators';
import { StepOrConnection } from '@syndesis/ui/platform';

import {
  ConnectionStore,
  ExtensionStore,
  StepStore,
  StepKind
} from '@syndesis/ui/store';

@Injectable({
  providedIn: 'root'
})
export class StepsResolverService implements Resolve<StepOrConnection[]> {
  constructor(
    private connectionStore: ConnectionStore,
    private extensionStore: ExtensionStore,
    private stepStore: StepStore
  ) {}

  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<any> {
    this.connectionStore.loadAll();
    this.extensionStore.loadAll();
    // Fetch available connections, fetch extensions, grab steps and return all of it
    return combineLatest(
      this.connectionStore.list.pipe(
        filter(c => typeof c !== 'undefined' && c.length > 0)
      ),
      this.extensionStore.list.pipe(
        filter(e => typeof e !== 'undefined'),
        map(extensions => this.stepStore.getSteps(extensions) as StepKind[])
      )
    ).pipe(
      switchMap(([connections, steps]) => {
        const allOfIt = [...connections, ...steps];
        return of(allOfIt as StepOrConnection[]);
      }),
      take(1)
    );
  }
}
