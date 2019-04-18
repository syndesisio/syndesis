import { Grid } from 'patternfly-react';
import * as React from 'react';
import { Container } from '../Layout';
import { InlineTextEdit } from '../Shared';
import './ConnectionDetailsHeader.css';

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
   * @param newDescription - the new description being saved
   * @returns `true` if save was successful
   */
  onChangeDescription: (newDescription: string) => Promise<boolean>;

  /**
   * The callback for when the connection name should be saved.
   * @param newName - the new name being saved
   * @returns `true` if save was successful
   */
  onChangeName: (newName: string) => Promise<boolean>;

  /**
   * The callback that validates the changes to the connection name. In case of error, the error message is expected as the return value.
   * @param proposedName - the proposed name being validated
   */
  validate: (proposedName: string) => true | string;
}

export interface IConnectionDetailsHeaderState {
  /**
   * `true` when the description is being edited.
   */
  editingDescription: boolean;

  /**
   * `true` when the name is being edited.
   */
  editingName: boolean;

  /**
   * `true` when the name or description is being saved.
   */
  isSaving: boolean;

  /**
   * The last persisted description.
   */
  prevDescription: string;

  /**
   * The last persisted name.
   */
  prevName: string;

  /**
   * The proposed new description.
   */
  proposedDescription: string;

  /**
   * The proposed new name.
   */
  proposedName: string;
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
      editingDescription: false,
      editingName: false,
      isSaving: false,
      prevDescription: currentDescription,
      prevName: this.props.connectionName,
      proposedDescription: this.props.connectionDescription
        ? this.props.connectionDescription
        : '',
      proposedName: this.props.connectionName,
    };
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
            <InlineTextEdit
              value={this.props.connectionName}
              i18nPlaceholder={this.props.i18nNamePlaceholder}
              isTextArea={false}
              smWidth={3}
              onChange={this.props.onChangeName}
              onValidate={this.props.validate}
            />
          </Grid.Col>
        </Grid.Row>
        <Grid.Row>
          <Grid.Col xs={2} className="connection-details-header__propertyLabel">
            {this.props.i18nDescriptionLabel}
          </Grid.Col>
          <Grid.Col>
            <InlineTextEdit
              value={this.props.connectionDescription || ''}
              i18nPlaceholder={this.props.i18nDescriptionPlaceholder}
              isTextArea={true}
              smOffset={2}
              smWidth={6}
              onChange={this.props.onChangeDescription}
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
