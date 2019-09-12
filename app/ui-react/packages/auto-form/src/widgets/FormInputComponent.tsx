import { FormGroup, TextInput } from '@patternfly/react-core';
import * as React from 'react';
import { IFormControlProps } from '../models';
import { FormLabelHintComponent } from './FormLabelHintComponent';
import { getHelperText, getValidationState, toValidHtmlId } from './helpers';

export const FormInputComponent: React.FunctionComponent<
  IFormControlProps
> = props => {
  const { value, onChange, ...field } = props.field;
  const id = toValidHtmlId(field.name);
  const handleChange = (
    _: string,
    event: React.FormEvent<HTMLInputElement>
  ) => {
    onChange(event);
  };
  const { helperText, helperTextInvalid } = getHelperText(
    props.field.name,
    props.property.description,
    props.form.errors
  );
  return (
    <FormGroup
      label={
        props.property.displayName ? (
          <>
            {props.property.displayName}
            {props.property.labelHint && (
              <FormLabelHintComponent labelHint={props.property.labelHint} />
            )}
          </>
        ) : (
          undefined
        )
      }
      {...props.property.formGroupAttributes}
      fieldId={id}
      isRequired={props.property.required}
      isValid={getValidationState(props)}
      helperText={helperText}
      helperTextInvalid={helperTextInvalid}
    >
      <TextInput
        {...props.property.fieldAttributes}
        {...field}
        value={value}
        data-testid={id}
        id={id}
        aria-label={props.property.displayName || field.name}
        isDisabled={props.form.isSubmitting || props.property.disabled}
        placeholder={props.property.placeholder}
        type={(props.type || 'text') as any}
        onChange={handleChange}
        title={props.property.controlHint}
        list={`${id}-list`}
      />
      {props.property.dataList && props.property.dataList.length > 0 && (
        <datalist id={`${id}-list`}>
          {props.property.dataList.map((opt, index) => (
            <option key={index} value={opt}>
              {opt}
            </option>
          ))}
        </datalist>
      )}
    </FormGroup>
  );
};
