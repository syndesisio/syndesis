import {
  ControlLabel,
  FieldLevelHelp,
  FormControl,
  FormGroup,
} from 'patternfly-react';
import * as React from 'react';
import { IFormControlProps } from '../models';
import { AutoFormHelpBlock } from './AutoFormHelpBlock';
import { getValidationState, toValidHtmlId } from './helpers';

export const FormTextAreaComponent: React.FunctionComponent<
  IFormControlProps
> = props => (
  <FormGroup
    {...props.property.formGroupAttributes}
    controlId={toValidHtmlId(props.field.name)}
    validationState={getValidationState(props)}
  >
    {props.property.displayName && (
      <ControlLabel
        className={
          props.property.required && !props.allFieldsRequired
            ? 'required-pf'
            : ''
        }
      >
        {props.property.displayName}
      </ControlLabel>
    )}
    {props.property.labelHint && (
      <ControlLabel>
        <FieldLevelHelp content={props.property.labelHint} />
      </ControlLabel>
    )}
    <FormControl
      {...props.property.fieldAttributes}
      {...props.field}
      data-testid={toValidHtmlId(props.field.name)}
      disabled={props.form.isSubmitting || props.property.disabled}
      componentClass="textarea"
      title={props.property.controlHint}
    />
    <AutoFormHelpBlock
      error={props.form.errors[props.field.name] as string}
      description={props.property.description}
    />
  </FormGroup>
);
