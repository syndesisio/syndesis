import {
  Button,
  Card,
  CardBody,
  CardFooter,
  CardHeader,
  Title,
} from '@patternfly/react-core';
import * as React from 'react';
import { Container, PageSection } from '../../../Layout';
import { ApiConnectorDetailConfig } from './ApiConnectorDetailConfig';
import { ApiConnectorDetailConfigEdit } from './ApiConnectorDetailConfigEdit';

export interface IApiConnectorDetailBodyProps {
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

export const ApiConnectorDetailBody: React.FunctionComponent<IApiConnectorDetailBodyProps> = ({
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
  // tslint:disable:no-console
  const isValid = false;
  const [isEditing, setIsEditing] = React.useState(false);

  const onCancelEditing = () => {
    console.log('cancelled editing...');
    setIsEditing(false);
  };

  const onStartEditing = () => {
    console.log('started editing from ApiConnectorDetailConfig..');
    setIsEditing(true);
  };

  const onSubmit = () => {
    console.log('submitted...');
    setIsEditing(false);
    if (handleSubmit) {
      handleSubmit();
    }
  };

  const preConfiguredProps = {
    address,
    basePath,
    description,
    host,
    icon,
    name,
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
              {isEditing ? (
                <ApiConnectorDetailConfigEdit
                  handleSubmit={onSubmit}
                  i18nCancelLabel={i18nCancelLabel}
                  i18nSaveLabel={i18nSaveLabel}
                  i18nValidateLabel={i18nValidateLabel}
                  onValidate={onValidate}
                  properties={preConfiguredProps}
                />
              ) : (
                <>
                  <ApiConnectorDetailConfig
                    i18nEditLabel={i18nEditLabel}
                    properties={preConfiguredProps}
                  />
                  <Button
                    data-testid={'connection-details-form-edit-button'}
                    variant="primary"
                    onClick={onStartEditing}
                  >
                    {i18nEditLabel}
                  </Button>
                </>
              )}
            </CardBody>
            {isEditing && (
              <CardFooter>
                <Button
                  data-testid={'connection-details-form-cancel-button'}
                  variant="secondary"
                  className="connection-details-form__editButton"
                  onClick={onCancelEditing}
                >
                  {i18nCancelLabel}
                </Button>
                <Button
                  data-testid={'connection-details-form-save-button'}
                  variant="primary"
                  className="connection-details-form__editButton"
                  disabled={!isValid}
                  onClick={onSubmit}
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
};
