import {
  Checkbox,
  FieldLevelHelp,
  FormGroup,
  HelpBlock,
} from 'patternfly-react';
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
      {...props.property.fieldAttributes}
      {...props.field}
      id={props.field.name}
      checked={props.field.value}
      data-testid={props.field.name}
      disabled={props.form.isSubmitting || props.property.disabled}
      title={props.property.controlHint}
    >
      {props.property.displayName}
      {props.property.labelHint && (
        <FieldLevelHelp
          className={'inline-block'}
          content={props.property.labelHint}
        />
      )}
    </Checkbox>
    <HelpBlock>
      {props.property.description}
      {props.form.errors[props.field.name]}
    </HelpBlock>
  </FormGroup>
);
