import {
  ControlLabel,
  DropdownButton,
  FieldLevelHelp,
  Form,
  FormGroup,
  MenuItem,
} from 'patternfly-react';
import * as React from 'react';
import { IFormControlProps } from '../models';
import { AutoFormHelpBlock } from './AutoFormHelpBlock';
import { getValidationState, toValidHtmlId } from './helpers';

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

export interface IFormDurationComponentState {
  duration: IDuration;
}

export class FormDurationComponent extends React.Component<
  IFormControlProps,
  IFormDurationComponentState
> {
  private inputField: HTMLInputElement = undefined as any;
  constructor(props: IFormControlProps) {
    super(props);
    // find the highest duration that keeps the duration above 1
    const index =
      durations.findIndex(d => !(this.props.field.value / d.value >= 1.0)) - 1;
    // if the index is invalid than we use the highest available duration.
    const duration = durations[index] || durations[durations.length - 1];
    this.state = {
      duration,
    };
    this.handleOnSelect = this.handleOnSelect.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.handleBlur = this.handleBlur.bind(this);
    this.receiveInputRef = this.receiveInputRef.bind(this);
  }
  public receiveInputRef(ref: HTMLInputElement) {
    this.inputField = ref;
  }
  public handleOnSelect(eventKey: number, event: React.ChangeEvent) {
    const newDuration =
      durations.find(duration => duration.value === eventKey) || durations[0];
    this.setState({
      duration: newDuration,
    });
    this.props.form.setFieldValue(
      this.props.field.name,
      calculateValue(newDuration, this.inputField.valueAsNumber),
      true
    );
  }
  public handleChange(event: React.ChangeEvent<HTMLInputElement>) {
    this.props.form.setFieldValue(
      this.props.field.name,
      calculateValue(this.state.duration, event.target.valueAsNumber),
      true
    );
  }
  public handleBlur(event: React.ChangeEvent<HTMLInputElement>) {
    this.props.form.setFieldValue(
      this.props.field.name,
      calculateValue(this.state.duration, event.target.valueAsNumber),
      true
    );
  }
  public render() {
    return (
      <FormGroup
        {...this.props.property.formGroupAttributes}
        controlId={toValidHtmlId(this.props.field.name)}
        validationState={getValidationState(this.props)}
      >
        <ControlLabel
          className={
            this.props.property.required && !this.props.allFieldsRequired
              ? 'required-pf'
              : ''
          }
          {...this.props.property.controlLabelAttributes}
        >
          {this.props.property.displayName}
        </ControlLabel>
        {this.props.property.labelHint && (
          <ControlLabel>
            <FieldLevelHelp content={this.props.property.labelHint} />
          </ControlLabel>
        )}
        <Form.InputGroup>
          <Form.FormControl
            min={0}
            {...this.props.property.fieldAttributes}
            data-testid={toValidHtmlId(this.props.field.name)}
            type={'number'}
            defaultValue={calculateDuration(
              this.state.duration,
              this.props.field.value
            )}
            disabled={
              this.props.form.isSubmitting || this.props.property.disabled
            }
            onChange={this.handleChange}
            onBlur={this.handleBlur}
            inputRef={this.receiveInputRef}
            title={this.props.property.controlHint}
          />
          <DropdownButton
            id={`${toValidHtmlId(this.props.field.name)}-duration`}
            data-testid={`${toValidHtmlId(this.props.field.name)}-duration`}
            componentClass={Form.InputGroup.Button}
            title={this.state.duration.label}
            onSelect={this.handleOnSelect}
            disabled={
              this.props.form.isSubmitting || this.props.property.disabled
            }
          >
            {durations.map((duration, index) => (
              <MenuItem key={index} eventKey={duration.value}>
                {duration.label}
              </MenuItem>
            ))}
          </DropdownButton>
        </Form.InputGroup>
        <AutoFormHelpBlock
          error={this.props.form.errors[this.props.field.name] as string}
          description={this.props.property.description}
        />
      </FormGroup>
    );
  }
}
