import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  Resolve,
  Router,
  RouterStateSnapshot
} from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { filter, mergeMap, take } from 'rxjs/operators';
import { Integration } from '@syndesis/ui/platform';
import { IntegrationStore } from '@syndesis/ui/store';

@Injectable({
  providedIn: 'root'
})
export class IntegrationResolverService implements Resolve<Integration> {
  constructor(
    private integrationStore: IntegrationStore,
    private router: Router
  ) {}

  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<Integration> | Observable<never> {
    this.integrationStore.clear();
    const id = route.paramMap.get('integrationId');
    return this.integrationStore.loadOrCreate(id).pipe(
      filter(integration => integration && 'flows' in integration),
      take(1),
      mergeMap(integration => {
        if (integration) {
          return of(integration);
        } else {
          this.router.navigate(['/integrations']);
          return EMPTY;
        }
      })
    );
  }
}
