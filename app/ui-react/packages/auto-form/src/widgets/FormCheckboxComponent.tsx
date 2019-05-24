import { Checkbox, FieldLevelHelp, FormGroup } from 'patternfly-react';
import * as React from 'react';
import { IFormControlProps } from '../models';
import { AutoFormHelpBlock } from './AutoFormHelpBlock';
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
      checked={props.field.value}
      data-testid={toValidHtmlId(props.field.name)}
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
    <AutoFormHelpBlock
      error={props.form.errors[props.field.name] as string}
      description={props.property.description}
    />
  </FormGroup>
);
