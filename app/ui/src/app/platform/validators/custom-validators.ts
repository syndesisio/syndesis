import { ValidatorFn, AbstractControl } from '@angular/forms';

export class CustomValidators {
  static requiredIfValidator(isRequired: boolean): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } => {
      return isRequired && !control.value ? { requiredIfChecked: true } : null;
    };
  }

  static validUrl(control: AbstractControl) {
    /* tslint:disable:max-line-length*/
    const regex = /\b((?:[a-z][\w-]+:(?:\/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}\/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'".,<>?«»“”‘’]))/i;
    /* tslint:enable:max-line-length*/

    if (!regex.test(control.value)) {
      return { validUrl: true };
    }
    return null;
  }
}
