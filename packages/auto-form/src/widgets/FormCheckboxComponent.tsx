import { Checkbox, FormGroup, HelpBlock } from 'patternfly-react';
import * as React from 'react';
import { IFormControl } from '../models';
import { getValidationState } from './helpers';

export const FormCheckboxComponent: React.FunctionComponent<
  IFormControl
> = props => (
  <FormGroup
    controlId={props.field.name}
    validationState={getValidationState(props)}
  >
    <Checkbox
      {...props.field}
      id={props.field.name}
      checked={props.field.value}
      data-testid={props.field.name}
      disabled={props.form.isSubmitting || props.property.disabled}
    >
      {props.property.displayName}
    </Checkbox>
    <HelpBlock>
      {props.property.description}
      {props.form.errors[props.field.name]}
    </HelpBlock>
  </FormGroup>
);
