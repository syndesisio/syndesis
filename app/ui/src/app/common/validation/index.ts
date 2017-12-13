import { Type } from '@angular/core';

import { RequiredIfValidatorDirective, requiredIfValidator } from './required-if-validator.directive';

export const SYNDESYS_VALIDATION_DIRECTIVES: Type<any>[] = [
  RequiredIfValidatorDirective
];

export {
  RequiredIfValidatorDirective, requiredIfValidator,
  // ... Place other validator and valdiator directives grouped by type below
};
