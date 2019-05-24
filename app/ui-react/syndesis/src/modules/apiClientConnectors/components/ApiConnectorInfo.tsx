import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { Connector } from '@syndesis/models';
import { ApiConnectorDetailsForm } from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';
import i18n from '../../../i18n';

export interface IApiConnectorInfoProps {
  /**
   * The API client connector whose information is being displayed.
   */
  apiConnector: Connector;

  /**
   * The connector icon.
   */
  apiConnectorIcon?: string;

  /**
   * `true` when the connection details are being edited.
   */
  isEditing: boolean;

  /**
   * `true` when the connection details are being saved.
   */
  isWorking: boolean;

  /**
   * The callback fired when submitting the form.
   * @param e the changed properties
   * @param actions used to set isSubmitting on the form
   */
  handleSubmit: (e?: any, actions?: any) => void;

  /**
   * The callback for editing has been canceled.
   */
  onCancelEditing: () => void;

  /**
   * The callback for start editing.
   */
  onStartEditing: () => void;

  /**
   * The callback for when an icon image is uploaded.
   */
  onUploadImage: (event: any) => void;
}

export class ApiConnectorInfo extends React.Component<IApiConnectorInfoProps> {
  public render() {
    // tslint:disable: object-literal-sort-keys
    const formDefinition = {
      name: {
        defaultValue: '',
        displayName: i18n.t('shared:Name'),
        required: true,
        type: 'string',
      },
      description: {
        defaultValue: '',
        displayName: i18n.t('shared:Description'),
        type: 'textarea',
      },
      host: {
        defaultValue: '',
        displayName: i18n.t('apiClientConnectors:Host'),
        type: 'string',
      },
      basePath: {
        defaultValue: '',
        displayName: i18n.t('apiClientConnectors:basePath'),
        type: 'string',
      },
    } as IFormDefinition;

    return (
      <Translation ns={['apiClientConnectors', 'shared']}>
        {t => (
          <AutoForm<{ [key: string]: string }>
            i18nRequiredProperty={t('shared:requiredFieldMessage')}
            definition={formDefinition}
            initialValue={{
              name: this.props.apiConnector.name,
              description: this.props.apiConnector.description || '',
              host: this.props.apiConnector.configuredProperties
                ? this.props.apiConnector.configuredProperties.host
                  ? this.props.apiConnector.configuredProperties.host
                  : ''
                : '',
              basePath: this.props.apiConnector.configuredProperties
                ? this.props.apiConnector.configuredProperties.basePath
                  ? this.props.apiConnector.configuredProperties.basePath
                  : ''
                : '',
            }}
            onSave={this.props.handleSubmit}
          >
            {({ fields, handleSubmit }) => (
              <ApiConnectorDetailsForm
                apiConnectorIcon={
                  this.props.apiConnectorIcon || this.props.apiConnector.icon
                }
                apiConnectorName={this.props.apiConnector.name}
                i18nCancelLabel={t('shared:Cancel')}
                i18nEditLabel={t('shared:Edit')}
                i18nIconLabel={t('ConnectorIcon')}
                i18nSaveLabel={t('shared:Save')}
                isEditing={this.props.isEditing}
                isWorking={this.props.isWorking}
                handleSubmit={handleSubmit}
                onCancelEditing={this.props.onCancelEditing}
                onStartEditing={this.props.onStartEditing}
                onUploadImage={this.props.onUploadImage}
              >
                {fields}
              </ApiConnectorDetailsForm>
            )}
          </AutoForm>
        )}
      </Translation>
    );
  }
}
