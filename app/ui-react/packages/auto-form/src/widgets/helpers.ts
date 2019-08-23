import { getIn } from 'formik';
import { IFormControlProps } from '../models';

export function getValidationState({ form, field }: IFormControlProps) {
  const error = getErrorText(form.errors, field.name);
  const touched = getIn(form.touched, field.name);
  return touched && error ? false : touched ? undefined : undefined;
}

export function getErrorText(errors: any, fieldName: string) {
  return getIn(errors, fieldName) || errors[fieldName];
}

export function getHelperText(
  fieldName: string,
  description: string | undefined,
  errors: any
) {
  const helperText = description || '';
  const errorText = getErrorText(errors, fieldName);
  const helperTextInvalid =
    helperText !== '' ? `${helperText} - ${errorText}` : errorText;
  return { helperText, helperTextInvalid };
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
