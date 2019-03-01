import {
  ControlLabel,
  FormControl,
  FormGroup,
  HelpBlock,
} from 'patternfly-react';
import * as React from 'react';
import { IFormControl } from '../models';
import { getValidationState } from './helpers';

export const FormSelectComponent: React.FunctionComponent<
  IFormControl
> = props => (
  <FormGroup
    controlId={props.field.name}
    validationState={getValidationState(props)}
  >
    <ControlLabel>{props.property.displayName}</ControlLabel>
    <FormControl
      {...props.field}
      data-testid={props.field.name}
      disabled={props.form.isSubmitting || props.property.disabled}
      componentClass="select"
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
