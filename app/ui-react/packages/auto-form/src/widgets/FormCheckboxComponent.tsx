import { Popover } from '@patternfly/react-core';
import { OutlinedQuestionCircleIcon } from '@patternfly/react-icons';
import { Checkbox, ControlLabel, FormGroup } from 'patternfly-react';
import * as React from 'react';
import { IFormControlProps } from '../models';
import { AutoFormHelpBlock } from './AutoFormHelpBlock';
import './FormCheckboxComponent.css';
import { getValidationState, toValidHtmlId } from './helpers';

export const FormCheckboxComponent: React.FunctionComponent<
  IFormControlProps
> = props => (
  <FormGroup
    {...props.property.formGroupAttributes}
    controlId={toValidHtmlId(props.field.name)}
    validationState={getValidationState(props)}
  >
    <Checkbox
      {...props.property.fieldAttributes}
      {...props.field}
      className="pf-u-display-inline-block"
      checked={props.field.value}
      id={toValidHtmlId(props.field.name)}
      data-testid={toValidHtmlId(props.field.name)}
      disabled={props.form.isSubmitting || props.property.disabled}
      title={props.property.controlHint}
    />
    <ControlLabel htmlFor={toValidHtmlId(props.field.name)}>
      {props.property.displayName}
    </ControlLabel>
    {props.property.labelHint && (
      <Popover
        aria-label={props.property.labelHint}
        bodyContent={props.property.labelHint}
      >
        <OutlinedQuestionCircleIcon className="pf-u-ml-xs" />
      </Popover>
    )}
    <AutoFormHelpBlock
      error={props.form.errors[props.field.name] as string}
      description={props.property.description}
    />
  </FormGroup>
);
