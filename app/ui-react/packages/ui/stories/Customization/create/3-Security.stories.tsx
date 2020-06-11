import { action } from '@storybook/addon-actions';
import { boolean } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import {
  ApiConnectorCreatorLayout,
  ICreateConnectorPropsUi,
} from '../../../src/Customization/apiClientConnectors';
import {
  ApiConnectorCreatorBreadSteps,
  ApiConnectorCreatorFooter,
  ApiConnectorCreatorSecurity,
  ApiConnectorCreatorToggleList,
} from '../../../src/Customization/apiClientConnectors/create';
import validateSecurity, {
  IErrorValidation,
} from '../../../src/Customization/apiClientConnectors/create/securityValidation';
import soapSpec from './soap';

const stories = storiesOf(
  'Customization/ApiClientConnector/CreateApiConnector/3 - Select Security',
  module
);

const preConfiguredValues: ICreateConnectorPropsUi = {
  authenticationType: soapSpec.properties!.authenticationType.defaultValue,
  authorizationEndpoint: soapSpec.properties!.authorizationEndpoint
    .defaultValue,
  passwordType: soapSpec.properties!.passwordType.defaultValue,
  tokenEndpoint: soapSpec.properties!.tokenEndpoint.defaultValue,
};

const dropdownOptions = {
  authenticationTypes: (
    soapSpec.properties!.authenticationType.enum || []
  ).sort((a, b) => a.value!.localeCompare(b.value!)),
  passwordTypes: (soapSpec.properties!.passwordType.enum || []).sort((a, b) =>
    a.value!.localeCompare(b.value!)
  ),
};

const component = (authenticationType: string) => {
  preConfiguredValues.authenticationType = authenticationType;

  const [errors, setErrors] = React.useState<IErrorValidation>({
    password: undefined,
    username: undefined,
  });
  const [values, setValues] = React.useState(preConfiguredValues);

  const handleChange = (param: any, event: any) => {
    const { checked, name, type } = event.target;
    const isCheckbox = type === 'checkbox';
    const value = isCheckbox ? checked : event.target.value;
    const isAuthType = name === 'authenticationType';

    let localValues: ICreateConnectorPropsUi;

    if (isAuthType) {
      localValues = { ...preConfiguredValues, [name]: value };
    } else {
      localValues = { ...values, [name]: value };

      if (name === 'passwordType') {
        localValues.addTimestamp = undefined;
        localValues.addUsernameTokenCreated = undefined;
        localValues.addUsernameTokenNonce = undefined;
        localValues.username = undefined;
        localValues.password = undefined;
      }
    }

    setValues(() => localValues);
    setErrors(() => validateSecurity(localValues));
  };

  const isValid = !errors.username && !errors.password;

  return (
    <ApiConnectorCreatorLayout
      content={
        <ApiConnectorCreatorSecurity
          dropdowns={dropdownOptions}
          handleChange={handleChange}
          i18nAccessTokenUrl={'Access Token URL'}
          i18nAuthenticationType={'Authentication Type'}
          i18nAuthorizationUrl={'Authorization URL'}
          i18nDescription={
            '$t(shared:project.name) reads the document to determine the information needed to configure the connector to meet the API’s security requirements. Connections created from this connector always use the authentication type that you select here.'
          }
          i18nNoSecurity={'No Security'}
          i18nPassword={'Password'}
          i18nPasswordType={'Password Type'}
          i18nTimestamp={'Timestamp'}
          i18nTitle={'Specify Security'}
          i18nUsername={'Username'}
          i18nUsernameTokenCreated={'Username Token Created'}
          i18nUsernameTokenNonce={'Username Token Nonce'}
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
          isNextDisabled={!isValid}
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
