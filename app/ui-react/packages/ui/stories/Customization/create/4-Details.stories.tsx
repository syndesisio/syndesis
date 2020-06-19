import { action } from '@storybook/addon-actions';
import { boolean } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import {
  ApiConnectorCreatorLayout,
  ApiConnectorDetailCard,
} from '../../../src';
import {
  ApiConnectorCreatorBreadSteps,
  ApiConnectorCreatorDetails,
  ApiConnectorCreatorFooter,
  ApiConnectorCreatorToggleList,
} from '../../../src/Customization/apiClientConnectors/create';
import icons from '../icons';

const stories = storiesOf(
  'Customization/ApiClientConnector/CreateApiConnector/4 - Details',
  module
);

stories.add('Review/Edit Connector Details', () => {
  return (
    <ApiConnectorCreatorLayout
      content={
        <div style={{ maxWidth: '600px' }}>
          <ApiConnectorCreatorDetails
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

const cancelLabel = 'Cancel';
const editLabel = 'Edit';
const iconLabel = 'Icon';
const name = 'MyApiConnector';
const saveLabel = 'Save';
const submitText = 'submit called';
const uploadImageText = 'upload called';

const storyNotes =
  '- Verify the form heading is "' +
  name +
  '"\n' +
  '- Verify the label for the icon is "' +
  iconLabel +
  '"\n' +
  '- Verify the "Choose File" button is disabled\n' +
  '- Verify the edit button is visible, enabled, and has text of "' +
  editLabel +
  '"\n' +
  '- Verify clicking the edit button prints out an edit message to the **Actions** tab\n' +
  '- Select the "isEditing" **Knob** and verify:\n' +
  '\t- the edit button is hidden\n' +
  '\t- the cancel button is visible, enabled, and has text of "' +
  cancelLabel +
  '"\n' +
  '\t- the save button is visible, enabled, and has text of "' +
  saveLabel +
  '"\n' +
  '\t- the "Choose File" button is enabled\n' +
  '\t- clicking the "isWorking" **Knob** displays a spinner on the save button\n' +
  '- Verify clicking the save button prints out a save message to the **Actions** tab\n' +
  '- Verify clicking the cancel button prints out a cancel message to the **Actions** tab\n';

stories.add(
  'ApiConnectorDetailsForm',
  () => (
    <ApiConnectorCreatorDetails
      apiConnectorIcon={icons.beer}
      apiConnectorName={name}
      i18nIconLabel={iconLabel}
      isEditing={boolean('isEditing', false)}
      handleSubmit={action(submitText)}
      onUploadImage={action(uploadImageText)}
      fields={<div>fields</div>}
      footer={<div>footer</div>}
    />
  ),
  { notes: storyNotes }
);

stories.add('ApiConnectorDetailCard', () => (
  <ApiConnectorDetailCard
    description={'An OpenAPI 2.0 version of the Beer API.'}
    icon={icons.beer}
    name={'Beer API 2.0'}
  />
));

stories.add('ApiConnectorDetailCard (no icon, no description)', () => (
  <ApiConnectorDetailCard name={name} />
));
