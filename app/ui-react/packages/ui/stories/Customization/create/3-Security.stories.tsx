import { action } from '@storybook/addon-actions';
import { boolean } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ApiConnectorCreatorLayout } from '../../../src/Customization/apiClientConnectors';
import {
  ApiConnectorCreatorBreadSteps,
  ApiConnectorCreatorFooter,
  ApiConnectorCreatorSecurity,
  ApiConnectorCreatorToggleList,
} from '../../../src/Customization/apiClientConnectors/create';
import soapSpec from './soap';

const stories = storiesOf(
  'Customization/ApiClientConnector/CreateApiConnector/3 - Select Security',
  module
);

const i18n = {
  accessTokenUrl: 'Access Token URL',
  address: 'Address',
  authenticationType: 'Authentication Type',
  authorizationUrl: 'Authorization URL',
  description:
    '$t(shared:project.name) reads the document to determine the information needed to configure the connector to meet the API’s security requirements. Connections created from this connector always use the authentication type that you select here.',
  noSecurity: 'No Security',
  password: 'Password',
  passwordType: 'Password Type',
  required: 'All fields are required.',
  timestamp: 'Timestamp',
  title: 'Specify Security',
  username: 'Username',
  usernameTokenCreated: 'Username Token Created',
  usernameTokenNonce: 'Username Token Nonce',
};

const preConfiguredValues = {
  authenticationType:
    soapSpec.properties!.authenticationType &&
    soapSpec.properties!.authenticationType.defaultValue,
  authorizationEndpoint:
    soapSpec.properties!.authorizationEndpoint &&
    soapSpec.properties!.authorizationEndpoint.defaultValue,
  passwordType:
    soapSpec.properties!.passwordType &&
    soapSpec.properties!.passwordType.defaultValue,
  tokenEndpoint:
    soapSpec.properties!.tokenEndpoint &&
    soapSpec.properties!.tokenEndpoint.defaultValue,
};

const dropdownOptions = {
  authenticationTypes:
    soapSpec.properties!.authenticationType &&
    (soapSpec.properties!.authenticationType.enum || []).sort((a, b) =>
      a.value!.localeCompare(b.value!)
    ),
  passwordTypes:
    soapSpec.properties!.passwordType &&
    (soapSpec.properties!.passwordType.enum || []).sort((a, b) =>
      a.value!.localeCompare(b.value!)
    ),
};

const component = (authenticationType: string) => {
  preConfiguredValues.authenticationType = authenticationType;

  const [values, setValues] = React.useState(preConfiguredValues);

  const handleChange = (param: any, event: any) => {
    const { checked, name, type } = event.target;

    // Checkboxes require special treatment
    const isCheckbox = type === 'checkbox';
    const value = isCheckbox ? checked : event.target.value;

    // If this is a change in the authentication type,
    // clear any previous values.
    const isAuthType = name === 'authenticationType';

    if (isAuthType) {
      setValues({ ...preConfiguredValues, [name]: value });
    } else {
      setValues({ ...values, [name]: value });
    }
  };

  return (
    <ApiConnectorCreatorLayout
      content={
        <ApiConnectorCreatorSecurity
          dropdowns={dropdownOptions}
          handleChange={handleChange}
          i18n={i18n}
          values={values}
        />
      }
      footer={
        <ApiConnectorCreatorFooter
          backHref={''}
          onNext={action('onNext')}
          i18nBack={'Back'}
          i18nNext={'Next'}
          isNextLoading={boolean('isNextLoading', false)}
          isNextDisabled={boolean('isNextDisabled', false)}
        />
      }
      navigation={
        <ApiConnectorCreatorBreadSteps
          step={3}
          i18nDetails={'Review/Edit Connector Details'}
          i18nReview={'Imported Operations'}
          i18nSecurity={'Specify Security'}
          i18nSelectMethod={'Provide Document'}
        />
      }
      toggle={
        <ApiConnectorCreatorToggleList
          step={1}
          i18nDetails={'Review/Edit Connector Details'}
          i18nReview={'Imported Operations'}
          i18nSecurity={'Specify Security'}
          i18nSelectMethod={'Provide Document'}
        />
      }
    />
  );
};

stories.add('SOAP Connector', () => {
  const initialSelectedType = 'basic';

  return component(initialSelectedType);
});
