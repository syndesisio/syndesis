import {
  FormGroup,
  FormSelect,
  FormSelectOption,
} from '@patternfly/react-core';
import * as React from 'react';
import { IFormControlProps } from '../models';
import { FormLabelHintComponent } from './FormLabelHintComponent';
import { getHelperText, getValidationState, toValidHtmlId } from './helpers';

import './FormSelectComponent.css';

function getSelectedValues(select: HTMLSelectElement) {
  return Array.from(select.selectedOptions).map(option => option.value);
}

export const FormSelectComponent: React.FunctionComponent<
  IFormControlProps
> = props => {
  const isMultiple =
    props.property.fieldAttributes && props.property.fieldAttributes.multiple;
  const { onChange, onBlur, value, ...field } = props.field;
  const id = toValidHtmlId(field.name);

  const adjustValue = () => {
    if (isMultiple) {
      let arrayValue = value;
      if (!value) {
        arrayValue = [];
      } else if (typeof value === 'string') {
        if (value.startsWith('[')) {
          arrayValue = JSON.parse(value);
        } else {
          arrayValue = [value];
        }
      }
      return arrayValue;
    } else {
      return value;
    }
  };

  const updatedValue = adjustValue();
  const handleChange = (
    eventValue: string,
    event: React.FormEvent<HTMLSelectElement>
  ) => {
    if (isMultiple) {
      const newValue = getSelectedValues(event.currentTarget);
      props.form.setFieldValue(props.field.name, newValue);
    } else {
      onChange(event);
    }
  };
  const handleBlur = (event: React.FormEvent<HTMLSelectElement>) =>
    handleChange('', event);
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
      <FormSelect
        size={isMultiple ? 12 : undefined}
        {...props.property.fieldAttributes}
        {...field}
        className={'autoform-select'}
        onChange={handleChange}
        onBlur={handleBlur}
        data-testid={id}
        id={id}
        aria-label={props.property.displayName || props.field.name}
        isDisabled={props.form.isSubmitting || props.property.disabled}
        isRequired={props.property.required}
        title={props.property.controlHint}
        value={updatedValue}
      >
        {(props.property.enum || []).map((opt: any, index: number) => (
          <FormSelectOption
            key={`${index}-${opt.label}`}
            value={opt.value}
            label={opt.label}
          />
        ))}
      </FormSelect>
    </FormGroup>
  );
};
