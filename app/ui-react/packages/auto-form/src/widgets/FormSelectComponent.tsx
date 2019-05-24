import {
  ControlLabel,
  FieldLevelHelp,
  FormControl,
  FormGroup,
} from 'patternfly-react';
import * as React from 'react';
import { IFormControlProps } from '../models';
import { AutoFormHelpBlock } from './AutoFormHelpBlock';
import { getValidationState, toValidHtmlId } from './helpers';

function getSelectedValues(select: HTMLSelectElement) {
  return Array.from(select.selectedOptions).map(option => option.value);
}

export const FormSelectComponent: React.FunctionComponent<
  IFormControlProps
> = props => {
  const isMultiple =
    props.property.fieldAttributes && props.property.fieldAttributes.multiple;
  const { onChange, onBlur, ...field } = props.field;
  const handleChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    if (isMultiple) {
      const newValue = getSelectedValues(event.currentTarget);
      props.form.setFieldValue(props.field.name, newValue);
    } else {
      onChange(event);
    }
  };
  return (
    <FormGroup
      {...props.property.formGroupAttributes}
      controlId={toValidHtmlId(field.name)}
      validationState={getValidationState(props)}
    >
      {props.property.displayName && (
        <ControlLabel
          className={
            props.property.required && !props.allFieldsRequired
              ? 'required-pf'
              : ''
          }
          {...props.property.controlLabelAttributes}
        >
          {props.property.displayName}
        </ControlLabel>
      )}
      {props.property.labelHint && (
        <ControlLabel>
          <FieldLevelHelp content={props.property.labelHint} />
        </ControlLabel>
      )}
      <FormControl
        size={isMultiple ? 12 : undefined}
        {...props.property.fieldAttributes}
        {...props.field}
        onChange={handleChange}
        onBlur={handleChange}
        data-testid={toValidHtmlId(props.field.name)}
        disabled={props.form.isSubmitting || props.property.disabled}
        componentClass="select"
        title={props.property.controlHint}
      >
        {(props.property.enum || []).map((opt: any, index: number) => (
          <option key={`${index}-${opt.label}`} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </FormControl>
      <AutoFormHelpBlock
        error={props.form.errors[props.field.name] as string}
        description={props.property.description}
      />
    </FormGroup>
  );
};
