import { Checkbox, FormGroup, HelpBlock } from 'patternfly-react';
import * as React from 'react';

export const FormCheckboxComponent = ({
  field,
  form: { isSubmitting },
  ...props
}: {
  [name: string]: any;
}) => (
  <FormGroup validationState={props.validationState}>
    <Checkbox
      {...field}
      id={field.name}
      checked={field.value}
      data-testid={field.name}
      disabled={isSubmitting || props.property.disabled}
      onChange={field.onChange}
    >
      {props.property.displayName}
    </Checkbox>
    <HelpBlock>{props.property.description}</HelpBlock>
  </FormGroup>
);
