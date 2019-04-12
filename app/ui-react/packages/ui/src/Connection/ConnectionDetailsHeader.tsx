import { AutoForm } from '@syndesis/auto-form';
import { Col, Grid, Icon, InlineEdit, Row } from 'patternfly-react';
import * as React from 'react';
import { Container } from '../Layout';
import './ConnectionDetailsHeader.css';

interface ISaveProp {
  prop: string;
}

export interface IConnectionDetailsHeaderProps {
  /**
   * The optional connection description.
   */
  connectionDescription?: string;

  /**
   * The optional connection icon.
   */
  connectionIcon?: string;

  /**
   * The name of the connection.
   */
  connectionName: string;

  /**
   * The localized text of the description label.
   */
  i18nDescriptionLabel: string;

  /**
   * The localized placeholder text of the connection description.
   */
  i18nDescriptionPlaceholder: string;

  /**
   * The localized validation message used when a required field is empty.
   */
  i18nIsRequiredMessage: string;

  /**
   * The localized placeholder text of the connection name.
   */
  i18nNamePlaceholder: string;

  /**
   * The localized text of the usage label.
   */
  i18nUsageLabel: string;

  /**
   * The localized message that provides the number of integrations that are using this connection.
   */
  i18nUsageMessage: string;

  /**
   * The callback for when the connection description should be saved.
   */
  onSaveDescription: (newDescription: string) => void;

  /**
   * The callback for when the connection name should be saved.
   */
  onSaveName: (newName: string) => void;
}

export interface IConnectionDetailsHeaderState {
  description: string;
  editingDescription: boolean;
  editingName: boolean;
  name: string;
  prevDescription: string;
  prevName: string;
}

/**
 * Line 1: icon, name, edit icon (makes name editable when clicked)
 * Line 2: description label and value
 * Line 3: usage label and value
 */
export class ConnectionDetailsHeader extends React.Component<
  IConnectionDetailsHeaderProps,
  IConnectionDetailsHeaderState
> {
  public constructor(props: IConnectionDetailsHeaderProps) {
    super(props);

    const currentDescription = this.props.connectionDescription
      ? this.props.connectionDescription
      : '';

    // setup initial state
    this.state = {
      description: currentDescription,
      editingDescription: false,
      editingName: false,
      name: this.props.connectionName,
      prevDescription: currentDescription,
      prevName: this.props.connectionName,
    };

    this.handleSaveDescription = this.handleSaveDescription.bind(this);
    this.handleSaveName = this.handleSaveName.bind(this);
    this.isEditingDescription = this.isEditingDescription.bind(this);
    this.isEditingName = this.isEditingName.bind(this);
    this.renderDescriptionEdit = this.renderDescriptionEdit.bind(this);
    this.renderDescriptionValue = this.renderDescriptionValue.bind(this);
    this.renderNameEdit = this.renderNameEdit.bind(this);
    this.renderNameValue = this.renderNameValue.bind(this);
    this.setEditingDescription = this.setEditingDescription.bind(this);
    this.setEditingName = this.setEditingName.bind(this);
    this.setNotEditingDescription = this.setNotEditingDescription.bind(this);
    this.setNotEditingName = this.setNotEditingName.bind(this);
  }

  public handleSaveDescription(newDescription: ISaveProp) {
    const proposedDescription = newDescription.prop;

    if (proposedDescription !== this.state.prevDescription) {
      this.props.onSaveDescription(proposedDescription);
      this.setState({
        ...this.state,
        description: proposedDescription,
        editingDescription: false,
        prevDescription: proposedDescription,
      });
    } else {
      this.setState({
        ...this.state,
        editingDescription: false,
      });
    }
  }

  public handleSaveName(newName: ISaveProp) {
    const proposedName = newName.prop;

    if (proposedName !== this.state.prevName) {
      this.props.onSaveName(proposedName);
      this.setState({
        ...this.state,
        editingName: false,
        name: proposedName,
        prevName: proposedName,
      });
    } else {
      this.setState({
        ...this.state,
        editingName: false,
      });
    }
  }

  public isEditingDescription() {
    return this.state.editingDescription;
  }

  public isEditingName() {
    return this.state.editingName;
  }

  public renderDescriptionEdit() {
    return (
      <AutoForm<ISaveProp>
        i18nRequiredProperty={this.props.i18nIsRequiredMessage}
        definition={{
          prop: {
            placeholder: this.props.i18nDescriptionPlaceholder,
            type: 'textarea',
          },
        }}
        initialValue={{
          prop: this.state.description,
        }}
        onSave={this.handleSaveDescription}
      >
        {({ fields, handleSubmit, values }) => (
          <Container>
            <Row>
              <Col sm={3}>{fields}</Col>
              <Col>
                <InlineEdit.ConfirmButton
                  // classNames="connection-details-header__saveButton"
                  disabled={values.prop === this.state.prevDescription}
                  onClick={handleSubmit}
                />
                <InlineEdit.CancelButton
                  onClick={this.setNotEditingDescription}
                />
              </Col>
            </Row>
          </Container>
        )}
      </AutoForm>
    );
  }

  public renderDescriptionValue() {
    return (
      <Container
        className={
          this.state.description.length > 0
            ? 'connection-details-header__propertyValue'
            : 'connection-details-header__placeholderText'
        }
      >
        {this.state.description.length > 0
          ? this.state.description
          : this.props.i18nDescriptionPlaceholder}
        <Icon
          type="pf"
          name="edit"
          onClick={this.setEditingDescription}
          className="connection-details-header__editIcon"
        />
      </Container>
    );
  }

  public renderNameEdit() {
    return (
      <AutoForm<ISaveProp>
        i18nRequiredProperty={this.props.i18nIsRequiredMessage}
        definition={{
          prop: {
            placeholder: this.props.i18nNamePlaceholder,
            required: true,
          },
        }}
        initialValue={{
          prop: this.state.name,
        }}
        validate={this.validateName}
        onSave={this.handleSaveName}
      >
        {({ fields, handleSubmit, values }) => (
          <Container>
            <Row>
              <Col sm={3}>{fields}</Col>
              <Col>
                <InlineEdit.ConfirmButton
                  // className="connection-details-header__saveButton"
                  disabled={values.prop === this.state.prevName}
                  onClick={handleSubmit}
                />
                <InlineEdit.CancelButton onClick={this.setNotEditingName} />
              </Col>
            </Row>
          </Container>
        )}
      </AutoForm>
    );
  }

  public renderNameValue() {
    return (
      <Container className="connection-details-header__connectionName">
        {this.state.name}
        <Icon
          type="pf"
          name="edit"
          onClick={this.setEditingName}
          className="connection-details-header__editIcon"
        />
      </Container>
    );
  }

  public setEditingDescription() {
    this.setState({ ...this.state, editingDescription: true });
  }

  public setEditingName() {
    this.setState({ ...this.state, editingName: true });
  }

  public setNotEditingDescription() {
    this.setState({ ...this.state, editingDescription: false });
  }

  public setNotEditingName() {
    this.setState({ ...this.state, editingName: false });
  }

  public validateName(proposedName: ISaveProp) {
    const errors: any = {};
    // TODO: Implement save here
    // if (v.virtName.includes('?')) {
    //   errors.virtName = 'Virtualization name contains an illegal character';
    // }

    return errors;
  }

  public render() {
    return (
      <Grid>
        <Grid.Row>
          {this.props.connectionIcon ? (
            <Grid.Col xs={1}>
              <Container className="blank-slate-pf-icon">
                <img
                  className="connection-details-header__connectionIcon"
                  src={this.props.connectionIcon}
                  alt={this.props.connectionName}
                  width={46}
                />
              </Container>
            </Grid.Col>
          ) : null}
          <Grid.Col>
            <InlineEdit
              value={this.props.connectionName}
              isEditing={this.isEditingName}
              renderValue={this.renderNameValue}
              renderEdit={this.renderNameEdit}
            />
          </Grid.Col>
        </Grid.Row>
        <Grid.Row>
          <Grid.Col xs={2} className="connection-details-header__propertyLabel">
            {this.props.i18nDescriptionLabel}
          </Grid.Col>
          <Grid.Col>
            <InlineEdit
              value={this.props.connectionDescription}
              isEditing={this.isEditingDescription}
              renderValue={this.renderDescriptionValue}
              renderEdit={this.renderDescriptionEdit}
            />
          </Grid.Col>
        </Grid.Row>
        <Grid.Row>
          <Grid.Col xs={2} className="connection-details-header__propertyLabel">
            {this.props.i18nUsageLabel}
          </Grid.Col>
          <Grid.Col className="connection-details-header__propertyValue">
            {this.props.i18nUsageMessage}
          </Grid.Col>
        </Grid.Row>
      </Grid>
    );
  }
}
