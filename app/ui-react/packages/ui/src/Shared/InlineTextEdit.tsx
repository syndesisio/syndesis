// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import {
  Col,
  FormControl,
  FormGroup,
  HelpBlock,
  Icon,
  InlineEdit,
  Row,
} from 'patternfly-react';
import * as React from 'react';
import { Container, Loader } from '../Layout';
import './InlineTextEdit.css';

interface IReadWidget {
  value: string;
  onEdit(): void;
}

const ReadWidget: React.FunctionComponent<IReadWidget> = ({
  value,
  onEdit,
}) => (
  <Container
    className={
      value.length > 0
        ? 'inline-text-edit__valueTextArea'
        : 'inline-text-edit__valueTextAreaPlaceholder'
    }
  >
    {value}
    <Icon
      className="inline-text-edit__editIcon" // not working
      name="edit"
      onClick={onEdit}
      type="pf"
    />
  </Container>
);

interface IEditWidget {
  valid: boolean;
  saving: boolean;
  value: string;
  asTextarea: boolean;
  placeholder?: string;
  errorMsg?: string;
  onChange(e: React.ChangeEvent): void;
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
}) =>
  asTextarea ? (
    <Container>
      <Row>
        <Col sm={6}>
          <FormGroup
            controlId="textarea"
            validationState={valid ? 'success' : 'error'}
          >
            <FormControl
              // className="inline-text-edit__valueTextArea"
              componentClass="textarea"
              disabled={saving}
              onChange={onChange}
              placeholder={placeholder}
              required={false}
              value={value}
            />
          </FormGroup>
          {errorMsg && <HelpBlock>{errorMsg}</HelpBlock>}
        </Col>
      </Row>
      <Row>
        <Col sm={6} xsOffset={2}>
          <Loader inline={true} loading={saving} />
          <InlineEdit.ConfirmButton
            disabled={saving || !valid}
            onClick={onConfirm}
          />
          <InlineEdit.CancelButton disabled={saving} onClick={onCancel} />
        </Col>
      </Row>
    </Container>
  ) : (
    <Container>
      <Row>
        <Col sm={3}>
          <FormGroup validationState={valid ? 'success' : 'error'}>
            <FormControl
              // className="inline-text-edit__valueText"
              disabled={saving}
              onChange={onChange}
              placeholder={placeholder}
              type="text"
              value={value}
            />
            {errorMsg && <HelpBlock>{errorMsg}</HelpBlock>}
          </FormGroup>
        </Col>
        <Col>
          <Loader inline={true} loading={saving} />
          <InlineEdit.ConfirmButton
            // className="connection-details-header__editButton connection-details-header__saveButton"
            disabled={saving || !valid}
            onClick={onConfirm}
          />
          <InlineEdit.CancelButton
            // className="connection-details-header__editButton"
            disabled={saving}
            onClick={onCancel}
          />
        </Col>
      </Row>
    </Container>
  );

export interface IInlineTextEditProps {
  /**
   * The current value of the property being rendered.
   */
  value: string;

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
  onValidate?: (newValue: string) => true | string;
}

export const InlineTextEdit: React.FunctionComponent<IInlineTextEditProps> = ({
  value,
  i18nPlaceholder,
  isTextArea,
  onChange,
  onValidate,
}) => {
  const [currentValue, setCurrentValue] = React.useState(value);
  const [editing, setEditing] = React.useState(false);
  const [saving, setSaving] = React.useState(false);
  const [{ valid, errorMsg }, setValidity] = React.useState({
    errorMsg: '',
    valid: true,
  });

  const validate = (valueToValidate: string) => {
    if (onValidate) {
      const result = onValidate(valueToValidate);
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

  const renderValue = (v: string) => <ReadWidget value={v} onEdit={onEdit} />;

  const renderEdit = (v: string) => (
    <EditWidget
      valid={valid}
      placeholder={i18nPlaceholder}
      saving={saving}
      value={v}
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
      value={currentValue}
      isEditing={isEditing}
      renderValue={renderValue}
      renderEdit={renderEdit}
    />
  );
};
