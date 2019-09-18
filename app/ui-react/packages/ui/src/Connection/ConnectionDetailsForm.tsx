import { Card, CardBody, CardFooter, CardHeader, Form, Title } from '@patternfly/react-core';
import { Alert, Button } from 'patternfly-react';
import * as React from 'react';
import { Container, Loader, PageSection } from '../Layout';
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
   * The callback for when the validate button is clicked.
   */
  onValidate: () => void;

  /**
   * `true` when the connection details are being edited.
   */
  isEditing: boolean;

  /**
   * The callback for editing has been canceled.
   */
  onCancelEditing: () => void;

  /**
   * The callback for start editing.
   */
  onStartEditing: () => void;
}

export class ConnectionDetailsForm extends React.Component<
  IConnectionDetailsFormProps
> {
  public static defaultProps = {
    validationResults: [],
  };

  public render() {
    return (
      <PageSection>
        <Container>
          <div className="row row-cards-pf">
            <Card>
              <CardHeader>
                <Title size="2xl">{this.props.i18nTitle}</Title>
              </CardHeader>
              <CardBody>
                <Form
                  isHorizontal={true}
                  data-testid={'connection-details-form'}
                  onSubmit={this.props.handleSubmit}
                >
                  {this.props.validationResults.map((e, idx) => (
                    <Alert key={idx} type={e.type}>
                      {e.message}
                    </Alert>
                  ))}
                  {this.props.children}
                  <div>
                    {this.props.isEditing ? (
                      <Button
                        data-testid={'connection-details-form-validate-button'}
                        bsStyle="default"
                        disabled={this.props.isWorking || !this.props.isValid}
                        onClick={this.props.onValidate}
                      >
                        {this.props.isWorking ? (
                          <Loader size={'sm'} inline={true} />
                        ) : null}
                        {this.props.i18nValidateLabel}
                      </Button>
                    ) : (
                      <Button
                        data-testid={'connection-details-form-edit-button'}
                        bsStyle="primary"
                        onClick={this.props.onStartEditing}
                      >
                        {this.props.i18nEditLabel}
                      </Button>
                    )}
                  </div>
                </Form>
              </CardBody>
              {this.props.isEditing ? (
                <CardFooter>
                  <Button
                    data-testid={'connection-details-form-cancel-button'}
                    bsStyle="default"
                    className="connection-details-form__editButton"
                    disabled={this.props.isWorking}
                    onClick={this.props.onCancelEditing}
                  >
                    {this.props.i18nCancelLabel}
                  </Button>
                  <Button
                    data-testid={'connection-details-form-save-button'}
                    bsStyle="primary"
                    className="connection-details-form__editButton"
                    disabled={this.props.isWorking || !this.props.isValid}
                    onClick={this.props.handleSubmit}
                  >
                    {this.props.i18nSaveLabel}
                  </Button>
                </CardFooter>
              ) : null}
            </Card>
          </div>
        </Container>
      </PageSection>
    );
  }
}
