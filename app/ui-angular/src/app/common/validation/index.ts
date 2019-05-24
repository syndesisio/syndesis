import { Type } from '@angular/core';

import { RequiredIfValidatorDirective } from '@syndesis/ui/common/validation/required-if-validator.directive';
import { ValidationErrorComponent } from '@syndesis/ui/common/validation/validation-error.component';

export const SYNDESYS_VALIDATION_DIRECTIVES: Type<any>[] = [
  RequiredIfValidatorDirective,
  ValidationErrorComponent
];
