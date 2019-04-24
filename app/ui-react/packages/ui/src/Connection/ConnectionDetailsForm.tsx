import { Alert, Button, Row } from 'patternfly-react';
import * as React from 'react';
import { Container, Loader } from '../Layout';
import './ConnectionDetailsForm.css';

export interface IConnectionDetailsValidationResult {
  message: string;
  type: 'error' | 'success';
}

export interface IConnectionDetailsFormProps {
  /**
   * The localized text for the cancel button.
   */
  i18nCancelLabel: string;

  /**
   * The localized text for the edit button.
   */
  i18nEditLabel: string;

  /**
   * The localized text for the save button.
   */
  i18nSaveLabel: string;

  /**
   * The localized text of the form title.
   */
  i18nTitle: string;

  /**
   * The localized text for the validate button.
   */
  i18nValidateLabel: string;

  /**
   * `true` if all form fields have valid values.
   */
  isValid: boolean;

  /**
   * `true` if the parent is doing some work and this form should disable user input.
   */
  isWorking: boolean;

  /**
   * Form level validationResults
   */
  validationResults: IConnectionDetailsValidationResult[];

  /**
   * The callback fired when submitting the form.
   * @param e
   */
  handleSubmit: (e?: any) => void;

  /**
   * The callback for editing has been canceled.
   */
  onCancel: () => void;

  /**
   * The callback for when the validate button is clicked.
   */
  onValidate: () => void;
}

export interface IConnectionDetailsFormState {
  /**
   * `true` when the connection details are being edited.
   */
  isEditing: boolean;
}

export class ConnectionDetailsForm extends React.Component<
  IConnectionDetailsFormProps,
  IConnectionDetailsFormState
> {
  public static defaultProps = {
    validationResults: [],
  };

  public constructor(props: IConnectionDetailsFormProps) {
    super(props);
    this.state = { isEditing: false };
    this.cancelEditing = this.cancelEditing.bind(this);
    this.startEditing = this.startEditing.bind(this);
  }

  public cancelEditing() {
    this.setState({ isEditing: false });
    this.props.onCancel();
  }

  public startEditing() {
    this.setState({ isEditing: true });
  }

  public render() {
    return (
      <Container>
        {this.props.isWorking ? <Loader size={'lg'} inline={true} /> : null}
        <form
          className="form-horizontal required-pf"
          role="form"
          onSubmit={this.props.handleSubmit}
        >
          <div className="row row-cards-pf">
            <div className="card-pf">
              {this.props.validationResults.map((e, idx) => (
                <Alert key={idx} type={e.type}>
                  {e.message}
                </Alert>
              ))}
              <div className="card-pf-title">{this.props.i18nTitle}</div>
              <div className="card-pf-body connection-details-form__formFields">
                <fieldset disabled={!this.state.isEditing}>
                  <Container>{this.props.children}</Container>
                </fieldset>
                {this.state.isEditing ? (
                  <Container>
                    <Row>
                      <Button
                        bsStyle="default"
                        className="connection-details-form__editButton"
                        disabled={this.props.isWorking}
                        onClick={this.props.onValidate}
                      >
                        {this.props.i18nValidateLabel}
                      </Button>
                    </Row>
                    <Row>
                      <Button
                        bsStyle="default"
                        className="connection-details-form__editButton"
                        disabled={this.props.isWorking}
                        onClick={this.cancelEditing}
                      >
                        {this.props.i18nCancelLabel}
                      </Button>
                      <Button
                        bsStyle="primary"
                        className="connection-details-form__editButton"
                        disabled={this.props.isWorking || !this.props.isValid}
                        onClick={this.props.handleSubmit}
                      >
                        {this.props.i18nSaveLabel}
                      </Button>
                    </Row>
                  </Container>
                ) : (
                  <Button bsStyle="primary" onClick={this.startEditing}>
                    {this.props.i18nEditLabel}
                  </Button>
                )}
              </div>
            </div>
          </div>
        </form>
      </Container>
    );
  }
}
