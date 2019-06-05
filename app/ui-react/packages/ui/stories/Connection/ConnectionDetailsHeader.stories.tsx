import { boolean } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ConnectionDetailsHeader } from '../../src';

const stories = storiesOf('Connection/ConnectionDetailsHeader', module);

const connectionDescription = 'This is my Salesforce connection';
const connectionName = 'Salesforce';
const desriptionLabel = 'Description';
const descriptionPlaceholder = 'Enter connection description...';
const namePlaceholder = 'Enter a connection name...';
const usageLabel = 'Usage';
const usageMessage = 'Used by 1 integration';

const changeDescription = (/*newDescription: string*/) => {
  return Promise.resolve(true);
};

const changeName = (/*newName: string*/) => {
  return Promise.resolve(true);
};

stories.add('render', () => {
  return (
    <ConnectionDetailsHeader
      allowEditing={boolean('allowEditing', true)}
      connectionDescription={connectionDescription}
      connectionIcon={<div />}
      connectionName={connectionName}
      i18nDescriptionLabel={desriptionLabel}
      i18nDescriptionPlaceholder={descriptionPlaceholder}
      i18nNamePlaceholder={namePlaceholder}
      i18nUsageLabel={usageLabel}
      i18nUsageMessage={usageMessage}
      isWorking={boolean('isWorking', false)}
      onChangeDescription={changeDescription}
      onChangeName={changeName}
    />
  );
});
