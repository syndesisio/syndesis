import {
  Alert,
  Button,
  Card,
  CardBody,
  CardFooter,
  CardHeader,
  Form,
  Title,
} from '@patternfly/react-core';
import * as React from 'react';
import { Container, Loader, PageSection } from '../Layout';
import { ERROR, WARNING } from '../Shared/models';
import './ConnectionDetailsForm.css';

export interface IConnectionDetailsValidationResult {
  message: string;
  type: 'error' | 'success' | 'info';
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
   * `true` if the connector has configuration fields, false if there are none
   */
  hasProperties: boolean;

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

export const ConnectionDetailsForm: React.FunctionComponent<IConnectionDetailsFormProps> =
  ({
    children,
    i18nCancelLabel,
    i18nEditLabel,
    i18nSaveLabel,
    i18nTitle,
    i18nValidateLabel,
    isValid,
    isWorking,
    hasProperties,
    validationResults,
    handleSubmit,
    onValidate,
    isEditing,
    onCancelEditing,
    onStartEditing,
  }) => (
    <PageSection>
      <Container>
        <div className="row row-cards-pf">
          <Card>
            <CardHeader>
              <Title size="2xl" headingLevel={'h3'}>
                {i18nTitle}
              </Title>
            </CardHeader>
            <CardBody>
              <Form
                isHorizontal={true}
                data-testid={'connection-details-form'}
                onSubmit={handleSubmit}
              >
                {validationResults.map((e, idx) => (
                  <Alert
                    title={e.message}
                    key={idx}
                    type={e.type === ERROR ? WARNING : e.type}
                  />
                ))}
                {children}
                <div>
                  {isEditing ? (
                    <Button
                      data-testid={'connection-details-form-validate-button'}
                      variant="secondary"
                      disabled={isWorking || !isValid}
                      onClick={onValidate}
                    >
                      {isWorking ? <Loader size={'sm'} inline={true} /> : null}
                      {i18nValidateLabel}
                    </Button>
                  ) : (
                    <>
                      {hasProperties && (
                        <Button
                          data-testid={'connection-details-form-edit-button'}
                          variant="primary"
                          onClick={onStartEditing}
                        >
                          {i18nEditLabel}
                        </Button>
                      )}
                    </>
                  )}
                </div>
              </Form>
            </CardBody>
            {isEditing && (
              <CardFooter>
                <Button
                  data-testid={'connection-details-form-cancel-button'}
                  variant="secondary"
                  className="connection-details-form__editButton"
                  disabled={isWorking}
                  onClick={onCancelEditing}
                >
                  {i18nCancelLabel}
                </Button>
                <Button
                  data-testid={'connection-details-form-save-button'}
                  variant="primary"
                  className="connection-details-form__editButton"
                  disabled={isWorking || !isValid}
                  onClick={handleSubmit}
                >
                  {i18nSaveLabel}
                </Button>
              </CardFooter>
            )}
          </Card>
        </div>
      </Container>
    </PageSection>
  );
