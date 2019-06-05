import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { ExtensionListItem } from '../../src';

const stories = storiesOf(
  'Customization/Extensions/Component/ExtensionListItem',
  module
);

const extensionDescription = 'Add a loop';
const extensionId = 'i-LWCfT7kHEGQFjGiGu8-z';
const extensionName = 'Loop';
const extensionType = 'Step Extension';

const deleteText = 'Delete';
const deleteActionText = deleteText + ' ' + extensionId;
const deleteTip = 'Delete this extension';
const detailsText = 'Details';
const detailsTip = 'View extension details';
const uid = 'uid';
const updateText = 'Update';
const updateActionText = updateText + ' ' + extensionId;
const updateTip = 'Update this extension';
const usedByFive = 5;
const usedByFiveMsg = 'Used by 5 integrations';
const usedByZero = 0;
const usedByZeroMsg = 'Not used by any integrations';

const inUseTestNotes =
  '- Verify extension icon is showing on the left\n' +
  '- Verify extension name is "' +
  extensionName +
  '"\n' +
  '- Verify extension description is "' +
  extensionDescription +
  '"\n' +
  '- Verify the extension type is "' +
  extensionType +
  '"\n' +
  '- Verify there is a details, update, and delete button\n' +
  '- Verify the details button is enabled\n' +
  '- Verify the details button text is "' +
  detailsText +
  '"\n' +
  '- Verify the details button tooltip is "' +
  detailsTip +
  '"\n' +
  '- Verify clicking the details button takes you to the extension details story. (hit browser back button)  \n"' +
  '- Verify the update button is enabled\n' +
  '- Verify the update button text is "' +
  updateText +
  '"\n' +
  '- Verify the update button tooltip is "' +
  updateTip +
  '"\n' +
  '- Verify clicking the update button prints "' +
  updateActionText +
  '" in the ACTION LOGGER\n' +
  '- Verify the delete button is disabled\n' +
  '- Verify the delete button text is "' +
  deleteText +
  '"\n' +
  '- Verify the delete button does not have a tooltip';

const notUsedTestNotes =
  '- Verify there is no extension icon on the left\n' +
  '- Verify extension name is "' +
  extensionName +
  '"\n' +
  '- Verify there is no extension description\n' +
  '- Verify the extension type is "' +
  extensionType +
  '"\n' +
  '- Verify there is a details, update, and delete button\n' +
  '- Verify the details button is enabled\n' +
  '- Verify the details button text is "' +
  detailsText +
  '"\n' +
  '- Verify the details button tooltip is "' +
  detailsText +
  '"\n' +
  '- Verify clicking the details button takes you to the extension details story. (hit browser back button)  \n"' +
  '- Verify the update button is enabled\n' +
  '- Verify the update button text is "' +
  updateText +
  '"\n' +
  '- Verify the update button tooltip is "' +
  updateText +
  '"\n' +
  '- Verify clicking the update button prints "' +
  updateActionText +
  '" in the ACTION LOGGER\n' +
  '- Verify the delete button is enabled\n' +
  '- Verify the delete button text is "' +
  deleteText +
  '"\n' +
  '- Verify the delete button tooltip is "' +
  deleteText +
  '"\n' +
  '- Verify clicking the delete button prints "' +
  deleteActionText +
  '" in the ACTION LOGGER';

stories
  .add(
    'in use',
    () => (
      <Router>
        <ExtensionListItem
          detailsPageLink={'/extensions/' + extensionId}
          extensionDescription={extensionDescription}
          extensionIcon={<div />}
          extensionId={extensionId}
          extensionName={extensionName}
          i18nCancelText={'Cancel'}
          i18nDelete={deleteText}
          i18nDeleteModalMessage={
            'Are you sure you want to delete the extension?'
          }
          i18nDeleteModalTitle={'Confirm Delete?'}
          i18nDeleteTip={deleteTip}
          i18nDetails={detailsText}
          i18nDetailsTip={detailsTip}
          i18nExtensionType={extensionType}
          i18nUpdate={updateText}
          i18nUpdateTip={updateTip}
          i18nUsedByMessage={usedByFiveMsg}
          linkUpdateExtension={uid}
          onDelete={action(deleteActionText)}
          usedBy={usedByFive}
        />
      </Router>
    ),
    { notes: inUseTestNotes }
  )
  .add(
    'not used',
    () => (
      <Router>
        <ExtensionListItem
          detailsPageLink={'/extensions/' + extensionId}
          extensionId={extensionId}
          extensionName={extensionName}
          extensionIcon={<div />}
          i18nCancelText={'Cancel'}
          i18nDelete={deleteText}
          i18nDeleteModalMessage={
            'Are you sure you want to delete the extension?'
          }
          i18nDeleteModalTitle={'Confirm Delete?'}
          i18nDetails={detailsText}
          i18nExtensionType={extensionType}
          i18nUpdate={updateText}
          i18nUsedByMessage={usedByZeroMsg}
          linkUpdateExtension={uid}
          onDelete={action(deleteActionText)}
          usedBy={usedByZero}
        />
      </Router>
    ),
    { notes: notUsedTestNotes }
  );
