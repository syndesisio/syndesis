import { Directive, Input } from '@angular/core';
import {
  AbstractControl,
  NG_VALIDATORS,
  Validator
} from '@angular/forms';

import { CustomValidators } from '@syndesis/ui/platform';

@Directive({
  selector: '[requiredIf]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: RequiredIfValidatorDirective,
      multi: true
    }
  ]
})
export class RequiredIfValidatorDirective implements Validator {
  @Input() requiredIf: boolean;

  validate(control: AbstractControl): { [key: string]: any } {
    return this.requiredIf
      ? CustomValidators.requiredIfValidator(this.requiredIf)(control)
      : null;
  }
}
