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

export const FormInputComponent: React.FunctionComponent<
  IFormControl
> = props => (
  <FormGroup
    controlId={props.field.name}
    validationState={getValidationState(props)}
  >
    <ControlLabel
      className={
        props.property.required && !props.allFieldsRequired ? 'required-pf' : ''
      }
    >
      {props.property.displayName}
    </ControlLabel>
    {props.property.labelHint && (
      <ControlLabel>
        <FieldLevelHelp content={props.property.labelHint} />
      </ControlLabel>
    )}
    <FormControl
      {...props.property.fieldAttributes}
      {...props.field}
      data-testid={props.field.name}
      disabled={props.form.isSubmitting || props.property.disabled}
      placeholder={props.property.placeholder}
      type={props.type || 'text'}
      onChange={props.field.onChange}
      title={props.property.controlHint}
    />
    <HelpBlock>
      {props.property.description}
      {props.form.errors[props.field.name]}
    </HelpBlock>
  </FormGroup>
);
