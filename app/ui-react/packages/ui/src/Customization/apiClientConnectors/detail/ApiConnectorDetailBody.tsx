import {
  Button,
  Card,
  CardBody,
  CardFooter,
  CardHeader,
  Title,
} from '@patternfly/react-core';
import * as React from 'react';
import { PageSection } from '../../../Layout';
import { ApiConnectorDetailConfig } from './ApiConnectorDetailConfig';
import { ApiConnectorDetailConfigEdit } from './ApiConnectorDetailConfigEdit';

export interface IApiConnectorDetailValues {
  address?: string;
  basePath?: string;
  description?: string;
  host?: string;
  icon?: string;
  name: string;
}

export interface IApiConnectorDetailBodyProps {
  /**
   * Configured Properties
   */
  address?: string;
  basePath?: string;
  description?: string;
  host?: string;
  icon?: string;
  name: string;

  /**
   * Property Labels
   */
  i18nLabelAddress: string;
  i18nLabelBaseUrl: string;
  i18nLabelDescription: string;
  i18nLabelHost: string;
  i18nLabelName: string;

  /**
   * The localized text for buttons
   */
  i18nCancelLabel: string;
  i18nEditLabel: string;
  i18nSaveLabel: string;

  i18nNameHelper: string;
  i18nTitle: string;
  i18nRequiredText: string;

  /**
   * The callback fired when submitting the form.
   * @param e
   */
  handleSubmit: (e: any) => void;

  /**
   * An array of strings with possible properties
   */
  propertyKeys: string[];
}

export const ApiConnectorDetailBody: React.FunctionComponent<IApiConnectorDetailBodyProps> =
  ({
    address,
    basePath,
    description,
    handleSubmit,
    host,
    i18nLabelAddress,
    i18nLabelBaseUrl,
    i18nLabelDescription,
    i18nLabelHost,
    i18nLabelName,
    i18nCancelLabel,
    i18nEditLabel,
    i18nNameHelper,
    i18nRequiredText,
    i18nSaveLabel,
    i18nTitle,
    icon,
    name,
    propertyKeys,
  }) => {
    const [configured, setConfigured] =
      React.useState<IApiConnectorDetailValues>({
        address,
        basePath,
        description,
        host,
        icon,
        name,
      });
    const [isEditing, setIsEditing] = React.useState(false);

    const onCancelEditing = () => {
      setConfigured({ address, basePath, description, host, icon, name });
      setIsEditing(false);
    };

    const onHandleChange = (fieldName: string, value: string) => {
      setConfigured({ ...configured, [fieldName]: value });
    };

    const onStartEditing = () => {
      setIsEditing(true);
    };

    const onSubmit = () => {
      setIsEditing(false);
      handleSubmit(configured);
    };

    return (
      <PageSection data-testid={'api-connector-detail-body'}>
        <Card>
          <CardHeader>
            <Title size={'2xl'} headingLevel={'h2'}>
              {i18nTitle}
            </Title>
          </CardHeader>
          <CardBody>
            {isEditing ? (
              <ApiConnectorDetailConfigEdit
                handleOnChange={onHandleChange}
                i18nLabelAddress={i18nLabelAddress}
                i18nLabelBaseUrl={i18nLabelBaseUrl}
                i18nLabelDescription={i18nLabelDescription}
                i18nLabelHost={i18nLabelHost}
                i18nLabelName={i18nLabelName}
                i18nNameHelper={i18nNameHelper}
                i18nRequiredText={i18nRequiredText}
                properties={configured}
                propertyKeys={propertyKeys}
              />
            ) : (
              <ApiConnectorDetailConfig
                i18nLabelAddress={i18nLabelAddress}
                i18nLabelBaseUrl={i18nLabelBaseUrl}
                i18nLabelDescription={i18nLabelDescription}
                i18nLabelHost={i18nLabelHost}
                i18nLabelName={i18nLabelName}
                properties={configured}
                propertyKeys={propertyKeys}
              />
            )}
          </CardBody>
          <CardFooter>
            {isEditing ? (
              <>
                <Button
                  data-testid={'connector-details-form-save-button'}
                  variant={'primary'}
                  isDisabled={!configured.name}
                  onClick={onSubmit}
                >
                  {i18nSaveLabel}
                </Button>{' '}
                <Button
                  data-testid={'connector-details-form-cancel-button'}
                  variant={'secondary'}
                  onClick={onCancelEditing}
                >
                  {i18nCancelLabel}
                </Button>
              </>
            ) : (
              <Button
                data-testid={'connector-details-form-edit-button'}
                variant={'primary'}
                onClick={onStartEditing}
              >
                {i18nEditLabel}
              </Button>
            )}
          </CardFooter>
        </Card>
      </PageSection>
    );
  };
