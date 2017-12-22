import { Directive, Input } from '@angular/core';
import { ValidatorFn, AbstractControl, NG_VALIDATORS, Validator } from '@angular/forms';

export function requiredIfValidator(isRequired: boolean): ValidatorFn {
  return (control: AbstractControl): {[key: string]: any} => {
    return isRequired && !control.value ? {'requiredIfChecked': true } : null;
  };
}

@Directive({
  selector: '[requiredIf]',
  providers: [{
    provide: NG_VALIDATORS,
    useExisting: RequiredIfValidatorDirective,
    multi: true
  }]
})
export class RequiredIfValidatorDirective implements Validator {
  @Input() requiredIf: boolean;

  validate(control: AbstractControl): { [key: string]: any } {
    return this.requiredIf ? requiredIfValidator(this.requiredIf)(control) : null;
  }
}
