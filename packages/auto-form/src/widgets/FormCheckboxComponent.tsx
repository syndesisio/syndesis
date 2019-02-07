import { Checkbox, FormGroup, HelpBlock } from 'patternfly-react';
import * as React from 'react';

export const FormCheckboxComponent = ({
  field,
  type,
  ...props
}: {
  [name: string]: any;
}) => (
  <FormGroup>
    <Checkbox
      {...field}
      id={field.name}
      checked={field.value}
      data-testid={field.name}
      onChange={field.onChange}
    >
      {props.property.displayName}
    </Checkbox>
    <HelpBlock>{props.property.description}</HelpBlock>
  </FormGroup>
);
