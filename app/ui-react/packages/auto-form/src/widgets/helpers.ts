import { IFormControl } from '../models';

export function getValidationState({ form, field }: IFormControl) {
  return form.touched[field.name] && form.errors[field.name]
    ? 'error'
    : form.touched[field.name]
    ? 'success'
    : null;
}
