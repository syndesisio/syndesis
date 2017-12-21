import { Type } from '@angular/core';

import { RequiredIfValidatorDirective, requiredIfValidator } from './required-if-validator.directive';
import { ValidationErrorComponent } from './validation-error.component';

export const SYNDESYS_VALIDATION_DIRECTIVES: Type<any>[] = [
  RequiredIfValidatorDirective,
  ValidationErrorComponent
];

export {
  requiredIfValidator,
  // ... Place other validator functions grouped by type below
};
