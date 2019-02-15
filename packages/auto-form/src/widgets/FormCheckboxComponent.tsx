import { Checkbox, FormGroup, HelpBlock } from 'patternfly-react';
import * as React from 'react';
import { IFormControl } from '../models';

export const FormCheckboxComponent = ({
  field,
  form: { isSubmitting },
  ...props
}: IFormControl) => (
  <FormGroup validationState={props.validationState}>
    <Checkbox
      {...field}
      id={field.name}
      checked={field.value}
      data-testid={field.name}
      disabled={isSubmitting || props.property.disabled}
    >
      {props.property.displayName}
    </Checkbox>
    <HelpBlock>{props.property.description}</HelpBlock>
  </FormGroup>
);
