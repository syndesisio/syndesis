import { FormGroup, Popover, TextInput } from '@patternfly/react-core';
import { OutlinedQuestionCircleIcon } from '@patternfly/react-icons';
import * as React from 'react';
import { IFormControlProps } from '../models';
import { getValidationState, toValidHtmlId } from './helpers';

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
  return (
    <FormGroup
      label={
        props.property.displayName ? (
          <>
            {props.property.displayName}
            {props.property.labelHint && (
              <Popover
                aria-label={props.property.labelHint}
                bodyContent={props.property.labelHint}
              >
                <OutlinedQuestionCircleIcon className="pf-u-ml-xs" />
              </Popover>
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
      helperText={props.property.description}
      helperTextInvalid={props.form.errors[props.field.name]}
    >
      <TextInput
        {...props.property.fieldAttributes}
        {...field}
        defaultValue={value}
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
