import { boolean } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ApiConnectorDetailsForm } from '../../src';

const stories = storiesOf('Customization/ApiConnectorDetailsForm', module);
const storyNotes = '- Verify something here';
const handleCancelEditing = () => {
  console.log('cancel editing');
};
const handleStartEditing = () => {
  console.log('start editing');
};
const handleSubmit = () => {
  console.log('submit');
};

stories.add(
  'render',
  () => (
    <ApiConnectorDetailsForm
      apiConnectorName="blah"
      i18nCancelLabel="Cancel"
      i18nEditLabel="Edit"
      i18nIconLabel="Icon"
      i18nSaveLabel="Save"
      isEditing={boolean('isEditing', false)}
      isWorking={boolean('isWorking', false)}
      handleSubmit={handleSubmit}
      onCancelEditing={handleCancelEditing}
      onStartEditing={handleStartEditing}
    />
  ),
  { notes: storyNotes }
);
