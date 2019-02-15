import {
  ControlLabel,
  FormControl,
  FormGroup,
  HelpBlock,
} from 'patternfly-react';
import * as React from 'react';
import { IFormControl } from '../models';

export const FormSelectComponent = ({
  field,
  form: { isSubmitting },
  ...props
}: IFormControl) => (
  <FormGroup controlId={field.name} validationState={props.validationState}>
    <ControlLabel>{props.property.displayName}</ControlLabel>
    <FormControl
      {...field}
      data-testid={field.name}
      disabled={isSubmitting || props.property.disabled}
      componentClass="select"
    >
      {props.property.enum &&
        props.property.enum.map((opt: any) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
    </FormControl>
    <HelpBlock>{props.property.description}</HelpBlock>
  </FormGroup>
);
