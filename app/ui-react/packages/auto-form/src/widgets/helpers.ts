import { IFormControlProps } from '../models';

export function getValidationState({ form, field }: IFormControlProps) {
  return form.touched[field.name] && form.errors[field.name]
    ? false
    : form.touched[field.name]
    ? undefined
    : undefined;
}

/**
 * Returns a valid DOM id from the given string
 * @param value
 */
export function toValidHtmlId(value?: string) {
  return value
    ? value.replace(/[^a-zA-Z0-9]+/g, '-').toLowerCase()
    : ((value || '') as string);
}
