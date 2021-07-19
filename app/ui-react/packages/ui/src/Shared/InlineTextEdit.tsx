// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import {
  Button,
  ButtonVariant,
  FormGroup,
  InputGroup,
  TextArea,
  TextInput,
  ValidatedOptions,
} from '@patternfly/react-core';
import { CheckIcon, PencilAltIcon, TimesIcon } from '@patternfly/react-icons';
import { global_palette_black_600 } from '@patternfly/react-tokens';
import classnames from 'classnames';
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
      <PencilAltIcon className="inline-text-editIcon" onClick={onEdit} />
    ) : null}
  </span>
);

interface IEditWidget
  extends React.InputHTMLAttributes<HTMLInputElement | HTMLTextAreaElement> {
  className?: string;
  value: string;
  valid: ValidatedOptions;
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
          fieldId="inline-edit-textarea"
          validated={valid}
          helperTextInvalid={errorMsg}
        >
          <InputGroup>
            <TextArea
              id={'inline-edit-textarea'}
              disabled={saving}
              onChange={(val, event) => onChange && onChange(event as any)}
              placeholder={placeholder}
              value={value}
            />
            <Button
              variant={ButtonVariant.control}
              isDisabled={saving || !valid}
              onClick={onConfirm}
            >
              {saving ? (
                <span className="btn">
                  <Loader inline={true} size={'sm'} />
                </span>
              ) : (
                <CheckIcon size={'sm'} color={global_palette_black_600.value} />
              )}
            </Button>
            <Button
              variant={ButtonVariant.control}
              isDisabled={saving}
              onClick={onCancel}
            >
              <TimesIcon size={'sm'} color={global_palette_black_600.value} />
            </Button>
          </InputGroup>
        </FormGroup>
      </>
    ) : (
      <>
        <FormGroup
          fieldId={'inline-edit-input'}
          validated={valid}
          helperTextInvalid={errorMsg}
        >
          <InputGroup>
            <TextInput
              isDisabled={saving}
              onChange={(val, event) => onChange && onChange(event as any)}
              placeholder={placeholder}
              type="text"
              value={value}
            />
            <Button
              variant={ButtonVariant.control}
              isDisabled={saving || !valid}
              onClick={onConfirm}
            >
              {saving ? (
                <span className="btn">
                  <Loader inline={true} size={'sm'} />
                </span>
              ) : (
                <CheckIcon size={'sm'} color={global_palette_black_600.value} />
              )}
            </Button>
            <Button
              variant={ButtonVariant.control}
              isDisabled={saving}
              onClick={onCancel}
            >
              <TimesIcon size={'sm'} color={global_palette_black_600.value} />
            </Button>
          </InputGroup>
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
  const [{ valid, errorMsg }, setValidity] = React.useState<{
    errorMsg: string;
    valid: ValidatedOptions;
  }>({
    errorMsg: 'error',
    valid: ValidatedOptions.default,
  });
  const validate = async (valueToValidate: string) => {
    if (onValidate) {
      const result = await onValidate(valueToValidate);
      if (result === true) {
        setValidity({
          errorMsg: '',
          valid: ValidatedOptions.default,
        });
      } else {
        setValidity({
          errorMsg: result,
          valid: ValidatedOptions.error,
        });
      }
    } else {
      setValidity({
        errorMsg: '',
        valid: ValidatedOptions.default,
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
  return <>{editing ? <>{renderEdit(value)}</> : <>{renderValue(value)}</>}</>;
};
