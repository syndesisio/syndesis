import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { ExtensionImportReview, IImportAction } from '../../src';

const stories = storiesOf(
  'Customization/Extensions/Component/ExtensionImportReview',
  module
);

const actions = [
  {
    description: 'The description for action 1',
    name: 'Action 1',
  } as IImportAction,
  {
    description: 'The description for action 2',
    name: 'Action 2',
  } as IImportAction,
  {
    description: 'The description for action 3',
    name: 'Action 3',
  } as IImportAction,
  {
    description: 'The description for action 4',
    name: 'Action 4',
  } as IImportAction,
] as IImportAction[];
const actionText = (theName: string, theDescription: string) => {
  return `<strong>${theName}</strong> - ${theDescription}`;
};
const cancelBtnText = 'Cancel';
const description = 'An extension to Syndesis to do Logging';
const descriptionLabel = 'Description';
const id = 'io.syndesis.extensions:syndesis-extension-log';
const idLabel = 'ID';
const importBtnText = 'Import Extension';
const name = 'Log';
const nameLabel = 'Name';
const stepsActionsLabel = 'Steps';
const title = 'Import Review';
const typeLabel = 'Type';
const typeMsg = 'Step Extension';
const uid = 'uid';

const handleImport = () => action('Import clicked');

const noDescriptionNotes =
  '- Verify title is "' +
  title +
  '"\n' +
  '- Verify ID label is "' +
  idLabel +
  '"\n' +
  '- Verify ID is "' +
  id +
  '"\n' +
  '- Verify description label is "' +
  descriptionLabel +
  '"\n' +
  '- Verify there is no description\n' +
  '- Verify type label is "' +
  typeLabel +
  '"\n' +
  '- Verify type message is "' +
  typeMsg +
  '"\n' +
  '- Verify actions label is "' +
  stepsActionsLabel +
  '"\n' +
  '- Verify there are ' +
  actions.length +
  ' actions';

const withDescriptionNotes =
  '- Verify title is "' +
  title +
  '"\n' +
  '- Verify ID label is "' +
  idLabel +
  '"\n' +
  '- Verify ID is "' +
  id +
  '"\n' +
  '- Verify description label is "' +
  descriptionLabel +
  '"\n' +
  '- Verify description is "' +
  description +
  '"\n' +
  '- Verify type label is "' +
  typeLabel +
  '"\n' +
  '- Verify type message is "' +
  typeMsg +
  '"\n' +
  '- Verify actions label is "' +
  stepsActionsLabel +
  '"\n' +
  '- Verify there are ' +
  actions.length +
  ' actions';

stories
  .add(
    'with description',
    () => (
      <Router>
        <ExtensionImportReview
          actions={actions}
          cancelLink={'/extensions'}
          extensionDescription={description}
          extensionId={id}
          extensionName={name}
          extensionUid={uid}
          i18nActionsLabel={stepsActionsLabel}
          i18nCancel={cancelBtnText}
          i18nDescriptionLabel={descriptionLabel}
          i18nExtensionTypeMessage={typeMsg}
          i18nIdLabel={idLabel}
          i18nImport={importBtnText}
          i18nNameLabel={nameLabel}
          i18nTitle={title}
          i18nTypeLabel={typeLabel}
          i18nActionText={actionText}
          onImport={handleImport}
        />
      </Router>
    ),
    { notes: withDescriptionNotes }
  )
  .add(
    'no description',
    () => (
      <Router>
        <ExtensionImportReview
          actions={actions}
          cancelLink={'/extensions'}
          extensionId={id}
          extensionName={name}
          extensionUid={uid}
          i18nActionsLabel={stepsActionsLabel}
          i18nCancel={cancelBtnText}
          i18nDescriptionLabel={descriptionLabel}
          i18nExtensionTypeMessage={typeMsg}
          i18nIdLabel={idLabel}
          i18nImport={importBtnText}
          i18nNameLabel={nameLabel}
          i18nTitle={title}
          i18nTypeLabel={typeLabel}
          i18nActionText={actionText}
          onImport={handleImport}
        />
      </Router>
    ),
    { notes: noDescriptionNotes }
  );
