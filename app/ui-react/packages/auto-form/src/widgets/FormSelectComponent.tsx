import {
  ControlLabel,
  FieldLevelHelp,
  FormControl,
  FormGroup,
  HelpBlock,
} from 'patternfly-react';
import * as React from 'react';
import { IFormControl } from '../models';
import { getValidationState } from './helpers';

function getSelectedValues(select: HTMLSelectElement) {
  return Array.from(select.selectedOptions).map(option => option.value);
}

export const FormSelectComponent: React.FunctionComponent<
  IFormControl
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
      controlId={field.name}
      validationState={getValidationState(props)}
    >
      <ControlLabel>{props.property.displayName}</ControlLabel>
      {props.property.labelHint && (
        <ControlLabel>
          <FieldLevelHelp content={props.property.labelHint} />
        </ControlLabel>
      )}
      <FormControl
        {...props.property.fieldAttributes}
        {...props.field}
        onChange={handleChange}
        onBlur={handleChange}
        data-testid={props.field.name}
        disabled={props.form.isSubmitting || props.property.disabled}
        componentClass="select"
        title={props.property.controlHint}
      >
        {props.property.enum &&
          props.property.enum.map((opt: any) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
      </FormControl>
      <HelpBlock>
        {props.property.description}
        {props.form.errors[props.field.name]}
      </HelpBlock>
    </FormGroup>
  );
};
