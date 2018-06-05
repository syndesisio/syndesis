import { Injectable } from '@angular/core';
import {
  CanDeactivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot
} from '@angular/router';
import { Observable } from 'rxjs';

/**
 * @see https://angular.io/guide/router#candeactivate-handling-unsaved-changes
 */
export interface CanComponentDeactivate {
  canDeactivate: (
    nextState: RouterStateSnapshot
  ) => Observable<boolean> | Promise<boolean> | boolean;
}

@Injectable()
export class CanDeactivateGuard
  implements CanDeactivate<CanComponentDeactivate> {
  canDeactivate(
    component: CanComponentDeactivate,
    currentRoute: ActivatedRouteSnapshot,
    currentState: RouterStateSnapshot,
    nextState: RouterStateSnapshot
  ) {
    return component.canDeactivate ? component.canDeactivate(nextState) : true;
  }
}
