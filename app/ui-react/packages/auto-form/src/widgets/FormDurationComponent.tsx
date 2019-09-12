import {
  Dropdown,
  DropdownItem,
  DropdownToggle,
  FormGroup,
  InputGroup,
  TextInput,
} from '@patternfly/react-core';
import * as React from 'react';
import { IFormControlProps } from '../models';
import { FormLabelHintComponent } from './FormLabelHintComponent';
import { getHelperText, getValidationState, toValidHtmlId } from './helpers';

interface IDuration {
  label: string;
  value: number;
}

const durations = [
  {
    label: 'Milliseconds',
    value: 1,
  },
  {
    label: 'Seconds',
    value: 1000,
  },
  {
    label: 'Minutes',
    value: 60000,
  },
  {
    label: 'Hours',
    value: 3600000,
  },
  {
    label: 'Days',
    value: 86400000,
  },
] as IDuration[];

function calculateDuration(duration: IDuration, initialValue: number) {
  return initialValue / duration.value;
}

function calculateValue(duration: IDuration, value: number) {
  return value * duration.value;
}

export const FormDurationComponent: React.FunctionComponent<
  IFormControlProps
> = props => {
  const { value, onChange, ...field } = props.field;
  // find the highest duration that keeps the duration above 1
  const index = durations.findIndex(d => !(value / d.value >= 1.0)) - 1;
  // if the index is invalid than we use the highest available duration.
  const initialDuration = durations[index] || durations[durations.length - 1];
  const [duration, setDuration] = React.useState(initialDuration);
  const [isOpen, setIsOpen] = React.useState(false);
  const handleToggle = () => {
    setIsOpen(!isOpen);
  };
  const handleClick = (
    selectedDuration: IDuration,
    event:
      | MouseEvent
      | React.MouseEvent<any, MouseEvent>
      | React.KeyboardEvent<Element>
  ) => {
    event.preventDefault();
    setIsOpen(false);
    const inputValue = calculateDuration(duration, props.field.value);
    setDuration(selectedDuration);
    props.form.setFieldValue(
      field.name,
      calculateValue(selectedDuration, inputValue),
      true
    );
  };
  const handleChange = (
    val: string,
    event: React.FormEvent<HTMLInputElement>
  ) => {
    props.form.setFieldValue(
      field.name,
      calculateValue(duration, parseInt(val, 10)),
      true
    );
  };
  const handleBlur = (event: React.ChangeEvent<HTMLInputElement>) => {
    props.form.setFieldValue(
      field.name,
      calculateValue(duration, event.target.valueAsNumber),
      true
    );
  };
  const id = toValidHtmlId(field.name);
  const controlId = `${id}-duration`;
  const { helperText, helperTextInvalid } = getHelperText(
    props.field.name,
    props.property.description,
    props.form.errors
  );
  return (
    <FormGroup
      label={
        <>
          {props.property.displayName || ''}
          {props.property.labelHint && (
            <FormLabelHintComponent labelHint={props.property.labelHint} />
          )}
        </>
      }
      {...props.property.formGroupAttributes}
      fieldId={id}
      isRequired={props.property.required}
      isValid={getValidationState(props)}
      helperText={helperText}
      helperTextInvalid={helperTextInvalid}
    >
      <InputGroup>
        <TextInput
          min={0}
          {...props.property.fieldAttributes}
          data-testid={id}
          id={id}
          type={'number'}
          defaultValue={`${calculateDuration(duration, props.field.value)}`}
          isDisabled={props.form.isSubmitting || props.property.disabled}
          onChange={handleChange}
          onBlur={handleBlur}
          title={props.property.controlHint}
        />
        <Dropdown
          data-testid={controlId}
          id={controlId}
          title={duration.label}
          toggle={
            <DropdownToggle onToggle={handleToggle}>
              {duration.label}
            </DropdownToggle>
          }
          isOpen={isOpen}
          disabled={props.form.isSubmitting || props.property.disabled}
        >
          {durations.map(d => (
            <DropdownItem
              key={d.value}
              value={d.value}
              component={'button'}
              onClick={e => handleClick(d, e)}
            >
              {d.label}
            </DropdownItem>
          ))}
        </Dropdown>
      </InputGroup>
    </FormGroup>
  );
};
