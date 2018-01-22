import { Type } from '@angular/core';
import { CanDeactivate } from '@angular/router';

import { CanDeactivateGuard, CanComponentDeactivate } from './can-deactivate.guard';

export {
  CanComponentDeactivate,
  CanDeactivateGuard
};

export const SYNDESIS_GUARDS: Type<CanDeactivate<any>>[] = [
  CanDeactivateGuard
];
