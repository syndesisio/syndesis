import { FormGroup, Popover, TextArea } from '@patternfly/react-core';
import { OutlinedQuestionCircleIcon } from '@patternfly/react-icons';
import * as React from 'react';
import { IFormControlProps } from '../models';
import { getHelperText, getValidationState, toValidHtmlId } from './helpers';

export const FormTextAreaComponent: React.FunctionComponent<
  IFormControlProps
> = props => {
  const { value, onChange, ...field } = props.field;
  const id = toValidHtmlId(field.name);
  const handleChange = (
    eventValue: string,
    event: React.ChangeEvent<HTMLTextAreaElement>
  ) => {
    onChange(event);
  };
  const { helperText, helperTextInvalid } = getHelperText(
    props.field.name,
    props.property.description,
    props.form.errors
  );
  return (
    <FormGroup
      label={
        props.property.displayName ? (
          <>
            {props.property.displayName || ''}
            {props.property.labelHint && (
              <Popover
                aria-label={props.property.labelHint}
                bodyContent={props.property.labelHint}
              >
                <OutlinedQuestionCircleIcon className="pf-u-ml-xs" />
              </Popover>
            )}
          </>
        ) : (
          undefined
        )
      }
      {...props.property.formGroupAttributes}
      fieldId={id}
      isRequired={props.property.required}
      isValid={getValidationState(props)}
      helperText={helperText}
      helperTextInvalid={helperTextInvalid}
    >
      <TextArea
        {...props.property.fieldAttributes}
        {...props.field}
        value={value}
        data-testid={id}
        id={id}
        aria-label={props.property.displayName || field.name}
        disabled={props.form.isSubmitting || props.property.disabled}
        onChange={handleChange}
        title={props.property.controlHint}
      />
    </FormGroup>
  );
};
