import {
  FormGroup,
  FormSelect,
  FormSelectOption,
  Popover,
} from '@patternfly/react-core';
import { OutlinedQuestionCircleIcon } from '@patternfly/react-icons';
import * as React from 'react';
import { IFormControlProps } from '../models';
import { getValidationState, toValidHtmlId } from './helpers';

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
  const updatedValue =
    isMultiple && typeof value === 'string' ? JSON.parse(value) : value;
  const handleChange = (
    _: string,
    event: React.ChangeEvent<HTMLSelectElement>
  ) => {
    if (isMultiple) {
      const newValue = getSelectedValues(event.currentTarget);
      props.form.setFieldValue(props.field.name, newValue);
    } else {
      onChange(event);
    }
  };
  const handleBlur = (event: React.ChangeEvent<HTMLSelectElement>) =>
    handleChange('', event);
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
