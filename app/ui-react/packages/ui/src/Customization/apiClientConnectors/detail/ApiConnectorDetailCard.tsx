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
import { Container, Loader, PageSection } from '../../../Layout';
import { ERROR, WARNING } from '../../../Shared';
import './ApiConnectorDetailCard.css';

export interface IApiConnectorDetailsValidationResult {
  message: string;
  type: 'error' | 'success' | 'info';
}

export interface IApiConnectorDetailCardProps {
  address?: string;
  basePath?: string;
  description?: string;
  host?: string;
  icon?: string;
  name: string;

  /**
   * The localized text for the cancel button.
   */
  i18nCancelLabel?: string;

  /**
   * The localized text for the edit button.
   */
  i18nEditLabel?: string;

  /**
   * The localized text for the save button.
   */
  i18nSaveLabel?: string;

  /**
   * The localized text of the form title.
   */
  i18nTitle?: string;

  /**
   * The localized text for the validate button.
   */
  i18nValidateLabel?: string;

  /**
   * The callback fired when submitting the form.
   * @param e
   */
  handleSubmit?: (e?: any) => void;

  /**
   * The callback for when the validate button is clicked.
   */
  onValidate?: () => void;
}

export const ApiConnectorDetailCard: React.FunctionComponent<IApiConnectorDetailCardProps> = ({
  address,
  basePath,
  description,
  handleSubmit,
  host,
  i18nCancelLabel,
  i18nEditLabel,
  i18nSaveLabel,
  i18nTitle,
  i18nValidateLabel,
  icon,
  name,
  onValidate,
}) => {
  const validationResults: IApiConnectorDetailsValidationResult[] = [
    {
      message: 'a success',
      type: 'success',
    },
    {
      message: 'something',
      type: 'info',
    },
    {
      message: 'uh oh',
      type: 'error',
    },
  ];

  const hasProperties = true;
  const isEditing = false;
  const isValid = false;
  const isWorking = false;
  // tslint:disable:no-console

  const onCancelEditing = () => {
    console.log('cancelled editing...');
  };

  const onStartEditing = () => {
    console.log('started editing...');
  };

  return (
    <PageSection>
      <Container>
        <div className="row row-cards-pf">
          <Card>
            <CardHeader>
              <Title size="2xl">{i18nTitle}</Title>
            </CardHeader>
            <CardBody>
              <Form
                isHorizontal={true}
                data-testid={'connection-details-form'}
                onSubmit={handleSubmit}
              >
                {validationResults!.map((e, idx) => (
                  <Alert
                    title={e.message}
                    key={idx}
                    type={e.type === ERROR ? WARNING : e.type}
                  />
                ))}
                <p>Children go here..</p>
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
    /*<Card className="api-connector-card">
      <CardBody>
        <div className={'api-connector-card__content'}>
          <div>
            <img className="api-connector-card__icon" src={icon} />
          </div>
          <div
            className="api-connector__title h2"
            data-testid={'api-connector-detail-card-title'}
          >
            {name}
          </div>
          <Text className="api-connector-card__description">{description}</Text>
        </div>
      </CardBody>
    </Card>*/
  );
};
