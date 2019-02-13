import {
  ControlLabel,
  FormControl,
  FormGroup,
  HelpBlock,
} from 'patternfly-react';
import * as React from 'react';

export const FormTextAreaComponent = ({
  field,
  form: { isSubmitting },
  ...props
}: {
  [name: string]: any;
}) => (
  <FormGroup controlId={field.name} validationState={props.validationState}>
    <ControlLabel>{props.property.displayName}</ControlLabel>
    <FormControl
      {...field}
      data-testid={field.name}
      disabled={isSubmitting || props.property.disabled}
      componentClass="textarea"
    />
    <HelpBlock>{props.property.description}</HelpBlock>
  </FormGroup>
);
