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

export interface IInlineTextEditProps {
  /**
   * The current value of the property being rendered.
   */
  currentValue?: string;

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
  onSave?: (newValue: string) => boolean;

  /**
   * The callback invoked when the proposed value changes.
   */
  onValidate?: (newValue: string) => true | string;
}

export interface IInlineTextEditState {
  /**
   * The current persisted value.
   */
  current: string;

  /**
   * `true` if the text is being edited.
   */
  editing: boolean;

  /**
   * The callback invoked when the proposed value should be saved.
   */
  isSaving: boolean;

  /**
   * The proposed new value.
   */
  proposed: string;

  /**
   * is the current value valid
   */
  valid: boolean;
  /**
   * An error message generated during validation of a proposed value.
   */
  errorMsg?: string;
}

export class InlineTextEdit extends React.Component<
  IInlineTextEditProps,
  IInlineTextEditState
> {
  public constructor(props: IInlineTextEditProps) {
    super(props);

    const current = this.props.currentValue ? this.props.currentValue : '';

    // setup initial state
    this.state = {
      current,
      editing: false,
      isSaving: false,
      proposed: current,
      valid: true,
    };

    this.handleSave = this.handleSave.bind(this);
    this.handleValueChanged = this.handleValueChanged.bind(this);
    this.isEditing = this.isEditing.bind(this);
    this.validate = this.validate.bind(this);
    this.renderEdit = this.renderEdit.bind(this);
    this.renderValue = this.renderValue.bind(this);
    this.setEditing = this.setEditing.bind(this);
    this.setNotEditing = this.setNotEditing.bind(this);
  }

  public componentDidMount(): void {
    this.validate(this.state.current);
  }

  public handleSave() {
    if (this.state.proposed !== this.state.current) {
      this.setState({ isSaving: true });

      if (this.props.onSave) {
        if (this.props.onSave(this.state.proposed)) {
          this.setState({
            current: this.state.proposed,
            editing: false,
            isSaving: false,
          });
        } else {
          this.setState({
            editing: false,
            isSaving: false,
          });
        }
      } else {
        this.setState({
          current: this.state.proposed,
          editing: false,
          isSaving: false,
        });
      }
    } else {
      this.setState({
        editing: false,
      });
    }
  }

  public handleValueChanged(e: any) {
    this.setState({
      proposed: e.target.value,
    });
    this.validate(e.target.value);
  }

  public isEditing(): boolean {
    return this.state.editing;
  }

  public validate(value: string) {
    if (this.props.onValidate) {
      const result = this.props.onValidate(value);
      this.setState({
        errorMsg: result === true ? undefined : result,
        valid: result === true,
      });
    }
  }

  public renderEdit() {
    return this.props.isTextArea ? (
      <Container>
        <Row>
          <Col sm={6}>
            <FormGroup controlId="textarea">
              <FormControl
                // className="inline-text-edit__valueTextArea"
                componentClass="textarea"
                disabled={this.state.isSaving}
                onChange={this.handleValueChanged}
                placeholder={this.props.i18nPlaceholder}
                required={false}
                value={this.state.proposed}
              />
            </FormGroup>
          </Col>
        </Row>
        <Row>
          <Col sm={6} xsOffset={2}>
            <Loader inline={true} loading={this.state.isSaving} />
            <InlineEdit.ConfirmButton
              disabled={
                this.state.isSaving ||
                this.state.proposed === this.state.current
              }
              onClick={this.handleSave}
            />
            <InlineEdit.CancelButton
              disabled={this.state.isSaving}
              onClick={this.setNotEditing}
            />
          </Col>
        </Row>
      </Container>
    ) : (
      <Container>
        <Row>
          <Col sm={3}>
            <FormGroup validationState={this.state.valid ? 'success' : 'error'}>
              <FormControl
                // className="inline-text-edit__valueText"
                disabled={this.state.isSaving}
                onChange={this.handleValueChanged}
                placeholder={this.props.i18nPlaceholder}
                type="text"
                value={this.state.proposed}
              />
              <HelpBlock>{this.state.errorMsg}</HelpBlock>
            </FormGroup>
          </Col>
          <Col>
            <Loader inline={true} loading={this.state.isSaving} />
            <InlineEdit.ConfirmButton
              // className="connection-details-header__editButton connection-details-header__saveButton"
              disabled={
                this.state.isSaving ||
                this.state.proposed === this.state.current
              }
              onClick={this.handleSave}
            />
            <InlineEdit.CancelButton
              // className="connection-details-header__editButton"
              disabled={this.state.isSaving}
              onClick={this.setNotEditing}
            />
          </Col>
        </Row>
      </Container>
    );
  }

  public renderValue() {
    return this.props.isTextArea ? (
      <Container className="inline-text-edit__valueText">
        {this.state.proposed}
        <Icon
          className="inline-text-edit__editIcon" // not working
          disabled={this.state.isSaving}
          name="edit"
          onClick={this.setEditing}
          type="pf"
        />
      </Container>
    ) : (
      <Container
        className={
          this.state.proposed.length > 0
            ? 'inline-text-edit__valueTextArea'
            : this.props.i18nPlaceholder
            ? 'inline-text-edit__valueTextAreaPlaceholder'
            : 'inline-text-edit__valueTextArea'
        }
      >
        {this.state.proposed.length > 0
          ? this.state.proposed
          : this.props.i18nPlaceholder
          ? this.props.i18nPlaceholder
          : ''}
        <Icon
          className="inline-text-edit__editIcon" // not working
          disabled={this.state.isSaving}
          name="edit"
          onClick={this.setEditing}
          type="pf"
        />
      </Container>
    );
  }

  public setEditing() {
    this.setState({ editing: true });
  }

  public setNotEditing() {
    this.setState({
      editing: false,
      proposed: this.state.current,
    });
  }

  public render() {
    return (
      <InlineEdit
        value={this.state.proposed}
        isEditing={this.isEditing}
        renderValue={this.renderValue}
        renderEdit={this.renderEdit}
      />
    );
  }
}
