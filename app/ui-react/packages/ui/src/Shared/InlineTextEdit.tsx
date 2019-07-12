// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import classnames from 'classnames';
import {
  FormControl,
  FormGroup,
  HelpBlock,
  Icon,
  InlineEdit,
  InputGroup,
} from 'patternfly-react';
import * as React from 'react';
import { Omit } from 'react-router';
import { Loader } from '../Layout';
import './InlineTextEdit.css';

interface IReadWidget {
  className?: string;
  allowEditing: boolean;
  value: string;
  onEdit(): void;
}

const ReadWidget: React.FunctionComponent<IReadWidget> = ({
  className,
  allowEditing,
  value,
  onEdit,
}) => (
  <span
    className={classnames('inline-text-readwidget', className)}
    onClick={allowEditing ? onEdit : undefined}
  >
    {value}
    {allowEditing ? (
      <Icon
        className="inline-text-editIcon"
        name="edit"
        onClick={onEdit}
        type="pf"
      />
    ) : null}
  </span>
);

interface IEditWidget extends React.InputHTMLAttributes<HTMLInputElement> {
  className?: string;
  value: string;
  valid: boolean;
  saving: boolean;
  asTextarea: boolean;
  errorMsg?: string;
  onConfirm(): void;
  onCancel(): void;
}

const EditWidget: React.FunctionComponent<IEditWidget> = ({
  valid,
  value,
  placeholder,
  errorMsg,
  saving,
  asTextarea,
  onChange,
  onConfirm,
  onCancel,
}) => (
  <div className={'inline-text-editwidget'}>
    {asTextarea ? (
      <>
        <FormGroup
          controlId="textarea"
          validationState={valid ? 'success' : 'error'}
        >
          <FormControl
            componentClass="textarea"
            disabled={saving}
            onChange={onChange}
            placeholder={placeholder}
            value={value}
          />
          {saving ? (
            <span className="btn">
              <Loader inline={true} loading={saving} size={'sm'} />
            </span>
          ) : (
            <InlineEdit.ConfirmButton
              disabled={saving || !valid}
              onClick={onConfirm}
            />
          )}
          <InlineEdit.CancelButton disabled={saving} onClick={onCancel} />
        </FormGroup>
        {errorMsg && <HelpBlock>{errorMsg}</HelpBlock>}
      </>
    ) : (
      <>
        <FormGroup validationState={valid ? 'success' : 'error'}>
          <InputGroup>
            <FormControl
              disabled={saving}
              onChange={onChange}
              placeholder={placeholder}
              type="text"
              value={value}
            />
            <InputGroup.Button>
              {saving ? (
                <span className="btn">
                  <Loader inline={true} loading={saving} size={'sm'} />
                </span>
              ) : (
                <InlineEdit.ConfirmButton
                  disabled={saving || !valid}
                  onClick={onConfirm}
                />
              )}
            </InputGroup.Button>
            <InputGroup.Button>
              <InlineEdit.CancelButton disabled={saving} onClick={onCancel} />
            </InputGroup.Button>
          </InputGroup>
          {errorMsg && <HelpBlock>{errorMsg}</HelpBlock>}
        </FormGroup>
      </>
    )}
  </div>
);

export interface IInlineTextEditProps
  extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'onChange'> {
  className?: string;

  /**
   * The current value of the property being rendered.
   */
  value: string;

  /**
   * `true` if editing is allowed.
   */
  allowEditing: boolean;

  /**
   * A value to display if the current value is empty or undefined.
   */
  i18nPlaceholder?: string;

  /**
   * `true` if the value should be rendered as a text area.
   */
  isTextArea: boolean;

  /**
   * The callback invoked when the confirm button is clicked.
   */
  onChange: (newValue: string) => Promise<boolean>;

  /**
   * The callback invoked when the proposed value changes.
   */
  onValidate?: (newValue: string) => Promise<true | string>;
}

export const InlineTextEdit: React.FunctionComponent<IInlineTextEditProps> = ({
  className,
  value,
  allowEditing,
  i18nPlaceholder,
  isTextArea,
  onChange,
  onValidate,
  ...attrs
}) => {
  const [currentValue, setCurrentValue] = React.useState(value);
  const [editing, setEditing] = React.useState(false);
  const [saving, setSaving] = React.useState(false);
  const [{ valid, errorMsg }, setValidity] = React.useState({
    errorMsg: '',
    valid: true,
  });

  const validate = async (valueToValidate: string) => {
    if (onValidate) {
      const result = await onValidate(valueToValidate);
      if (result === true) {
        setValidity({
          errorMsg: '',
          valid: true,
        });
      } else {
        setValidity({
          errorMsg: result,
          valid: false,
        });
      }
    } else {
      setValidity({
        errorMsg: '',
        valid: true,
      });
    }
  };

  const handleConfirm = async () => {
    if (valid) {
      setSaving(true);
      const success = await onChange(currentValue);
      if (success) {
        setEditing(false);
      }
      setSaving(false);
    }
  };

  const handleChange = (e: any) => {
    setCurrentValue(e.target.value);
    validate(e.target.value);
  };

  const onEdit = () => {
    setEditing(true);
    validate(currentValue);
  };

  const onCancel = () => {
    setEditing(false);
    setCurrentValue(value);
  };

  const renderValue = (v: string) => (
    <ReadWidget
      className={className}
      value={v || i18nPlaceholder || 'Value...'}
      allowEditing={allowEditing}
      onEdit={onEdit}
    />
  );

  const renderEdit = (v: string) => (
    <EditWidget
      {...attrs}
      valid={valid}
      saving={saving}
      value={currentValue}
      errorMsg={errorMsg}
      asTextarea={isTextArea}
      onChange={handleChange}
      onConfirm={handleConfirm}
      onCancel={onCancel}
    />
  );

  const isEditing = () => editing;

  return (
    <InlineEdit
      className={className}
      value={value}
      isEditing={isEditing}
      renderValue={renderValue}
      renderEdit={renderEdit}
    />
  );
};
