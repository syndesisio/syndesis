import { action } from '@storybook/addon-actions';
import { boolean } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ApiConnectorCreatorLayout } from '../../../src';
import {
  ApiClientConnectorCreateSecurity,
  ApiConnectorCreatorBreadSteps,
  ApiConnectorCreatorFooter,
  ApiConnectorCreatorToggleList,
} from '../../../src/Customization/apiClientConnectors/create';

const stories = storiesOf(
  'Customization/ApiClientConnector/CreateApiConnector/3 - Select Security',
  module
);

stories.add('Specify Security', () => (
  <ApiConnectorCreatorLayout
    content={
      <ApiClientConnectorCreateSecurity
        authenticationTypes={[]}
        authUrl={''}
        extractAuthType={(params?: string) => ''}
        handleChangeAuthUrl={action('handleChangeAuthUrl')}
        handleChangeSelectedType={action('handleChangeSelectedType')}
        handleChangeTokenUrl={action('handleChangeTokenUrl')}
        i18nAccessTokenUrl={'Access Token URL'}
        i18nAuthorizationUrl={
          'apiClientConnectors:create:security:authorizationUrl'
        }
        i18nDescription={'apiClientConnectors:create:security:description'}
        i18nNoSecurity={'apiClientConnectors:create:security:noSecurity'}
        i18nTitle={'Specify Security'}
        selectedType={'selectedType'}
        tokenUrl={'tokenUrl'}
      />
    }
    footer={
      <ApiConnectorCreatorFooter
        backHref={''}
        onNext={action('')}
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
));
