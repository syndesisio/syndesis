import { action } from '@storybook/addon-actions';
import { boolean } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ApiConnectorCreatorLayout } from '../../../src';
import { ApiConnectorDetailsForm } from '../../../src/Customization/apiClientConnectors';
import {
  ApiConnectorCreatorBreadSteps,
  ApiConnectorCreatorFooter,
  ApiConnectorCreatorToggleList,
} from '../../../src/Customization/apiClientConnectors/create';

const stories = storiesOf(
  'Customization/ApiClientConnector/CreateApiConnector/4 - Details',
  module
);

stories.add('Review/Edit Connector Details', () => {
  return (
    <ApiConnectorCreatorLayout
      content={
        <div style={{ maxWidth: '600px' }}>
          <ApiConnectorDetailsForm
            apiConnectorIcon={'icon'}
            apiConnectorName={'connectorName'}
            i18nIconLabel={'ConnectorIcon'}
            handleSubmit={action('handleSubmit')}
            onUploadImage={action('onUploadImage')}
            isEditing={boolean('isEditing', true)}
            fields={<></>}
          />
        </div>
      }
      footer={
        <ApiConnectorCreatorFooter
          backHref={''}
          onNext={action('submitForm')}
          i18nBack={'Back'}
          i18nNext={'Save'}
          isNextLoading={boolean('isSubmitting', false)}
          isNextDisabled={boolean('isNextDisabled', false)}
        />
      }
      navigation={
        <ApiConnectorCreatorBreadSteps
          step={4}
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
});
