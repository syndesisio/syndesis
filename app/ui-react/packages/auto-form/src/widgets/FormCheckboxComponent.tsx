import { Checkbox, FormGroup } from '@patternfly/react-core';
import * as React from 'react';
import { IFormControlProps } from '../models';
import './FormCheckboxComponent.css';
import { FormLabelHintComponent } from './FormLabelHintComponent';
import { getValidationState, toValidHtmlId } from './helpers';

export const FormCheckboxComponent: React.FunctionComponent<
  IFormControlProps
> = props => {
  const { value, onChange, ...field } = props.field;
  const id = toValidHtmlId(field.name);
  const handleChange = (
    val: boolean,
    event: React.FormEvent<HTMLInputElement>
  ) => {
    onChange(event);
  };
  return (
    <FormGroup
      label={props.property.displayNameCheckbox}
      {...props.property.formGroupAttributes}
      fieldId={id}
      isValid={getValidationState(props)}
      helperText={props.property.description}
      helperTextInvalid={props.form.errors[props.field.name]}
    >
      <Checkbox
        {...props.property.fieldAttributes}
        {...field}
        onChange={handleChange}
        aria-label={props.property.displayName || ''}
        label={<>&nbsp;{ /*TODO <<< workaround */ props.property.displayName}</>}
        className="pf-u-display-inline-block"
        isChecked={value}
        id={id}
        data-testid={id}
        isDisabled={props.form.isSubmitting || props.property.disabled}
        title={props.property.controlHint}
      />
      {props.property.labelHint && (
        <FormLabelHintComponent labelHint={props.property.labelHint} />
      )}
    </FormGroup>
  );
};
