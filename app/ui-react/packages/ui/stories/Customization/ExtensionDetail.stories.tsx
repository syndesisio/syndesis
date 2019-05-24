import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import {
  ExtensionDetail,
  ExtensionOverview,
  ExtensionSupports,
  IAction,
} from '../../src';

export const extensionDetailStory = 'step extension in use';

const stories = storiesOf(
  'Customization/Extensions/Component/ExtensionDetail',
  module
);

const extension = {
  actions: [
    {
      description: "This is Action 1's description",
      name: 'Action 1',
    } as IAction,
    {
      description: "This is Action 2's description",
      name: 'Action 2',
    } as IAction,
    {
      description: "This is Action 3's description",
      name: 'Action 3',
    } as IAction,
    {
      description: "This is Action 4's description",
      name: 'Action 4',
    } as IAction,
    {
      description: "This is Action 5's description",
      name: 'Action 5',
    } as IAction,
  ],
  description: 'Add a loop',
  extensionId: 'io.syndesis.extensions:syndesis-extension-loop',
  extensionType: 'Steps',
  name: 'Loop',
  uses: 2,
};
const notUsedExtension = {
  ...extension,
  description: undefined,
  extensionType: 'Connectors',
  uses: 0,
};
const libraryExtension = {
  ...extension,
  extensionType: 'Libraries',
};

const deleteText = 'Delete';
const deleteActionText = deleteText + ' ' + extension.name;
const deleteTip = 'Delete this extension';
const descriptionText = 'Description';
const idMsg = '(ID: ' + extension.extensionId + ')';
const lastUpdateText = 'Last Update';
const lastUpdateDate = 'Dec 10, 2018, 10:32:28 AM';
const nameText = 'Name';
const overviewText = 'Overview';
const supportedConnectorsText = 'Supported Connectors';
const supportedLibrariesText = 'Supported Libraries';
const supportedStepsText = 'Supported Steps';
const stepTypeMessage = 'Step Extension';
const connectorTypeMessage = 'Connector Extension';
const libraryTypeMessage = 'Library Extension';
const typeText = 'Type';
const updateText = 'Update';
const updateActionText = updateText + ' ' + extension.name;
const updateTip = 'Update this extension';
const usageText = 'Usage';

const inUseTestNotes =
  '- Verify the header section has the extension name of "' +
  extension.name +
  '"\n' +
  '- Verify the header section has the extension ID text of "' +
  idMsg +
  '"\n' +
  '- Verify the header section has an update and delete button\n' +
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
  '- Verify the delete button does not have a tooltip\n' +
  '- Verify the card body has sections for overview, supported steps, and usage\n' +
  '- Verify the overview section has a title of "' +
  overviewText +
  '"\n' +
  '- Verify the overview section has a name label with a title of "' +
  nameText +
  '"\n' +
  '- Verify the extension name is "' +
  extension.name +
  '"\n' +
  '- Verify the overview section has a description label with a title of "' +
  descriptionText +
  '"\n' +
  '- Verify the extension description is "' +
  extension.description +
  '"\n' +
  '- Verify the overview section has a type label with a title of "' +
  typeText +
  '"\n' +
  '- Verify the extension type is "' +
  stepTypeMessage +
  '"\n' +
  '- Verify the overview section has a last update label with a title of "' +
  lastUpdateText +
  '"\n' +
  '- Verify the extension last update date is "' +
  lastUpdateDate +
  '"\n' +
  '- Verify the supported steps section has a title of "' +
  supportedStepsText +
  '"\n' +
  '- Verify the supported steps section has 5 actions with the appropriate names and descriptions\n' +
  '- Verify the usage section has a title of "' +
  usageText;

const libraryTypeTestNotes =
  '- Verify the extension type is "' +
  libraryTypeMessage +
  '"\n' +
  '- Verify the supported steps section has a title of "' +
  supportedLibrariesText +
  '"';

const notUsedTestNotes =
  '- Verify the header section has the extension name of "' +
  extension.name +
  '"\n' +
  '- Verify the header section has the extension ID text of "' +
  idMsg +
  '"\n' +
  '- Verify the header section has an update and delete button\n' +
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
  deleteTip +
  '"\n' +
  '- Verify clicking the delete button prints "' +
  deleteActionText +
  '" in the ACTION LOGGER\n' +
  '- Verify the card body has sections for overview, supported steps, and usage\n' +
  '- Verify the overview section has a title of "' +
  overviewText +
  '"\n' +
  '- Verify the overview section has a name label with a title of "' +
  nameText +
  '"\n' +
  '- Verify the extension name is "' +
  extension.name +
  '"\n' +
  '- Verify the overview section has a description label with a title of "' +
  descriptionText +
  '"\n' +
  '- Verify the extension description is empty\n' +
  '- Verify the overview section has a type label with a title of "' +
  typeText +
  '"\n' +
  '- Verify the extension type is "' +
  connectorTypeMessage +
  '"\n' +
  '- Verify the overview section has a last update label with a title of "' +
  lastUpdateText +
  '"\n' +
  '- Verify the extension last update date is "' +
  lastUpdateDate +
  '"\n' +
  '- Verify the supported steps section has a title of "' +
  supportedConnectorsText +
  '"\n' +
  '- Verify the supported steps section has 5 actions with the appropriate names and descriptions\n' +
  '- Verify the usage section has a title of "' +
  usageText;

stories
  .add(
    extensionDetailStory,
    () => (
      <Router>
        <ExtensionDetail
          extensionName={extension.name}
          extensionUses={extension.uses}
          i18nCancelText={'Cancel'}
          i18nDelete={deleteText}
          i18nDeleteModalMessage={
            'Are you sure you want to delete the extension?'
          }
          i18nDeleteModalTitle={'Confirm Delete?'}
          i18nIdMessage={idMsg}
          i18nOverviewSectionTitle={overviewText}
          i18nSupportsSectionTitle={supportedStepsText}
          i18nUpdate={updateText}
          i18nUpdateTip={updateTip}
          i18nUsageSectionTitle={usageText}
          integrationsSection={<div />}
          linkUpdateExtension={'/extensions/'}
          onDelete={action(deleteActionText)}
          overviewSection={
            <ExtensionOverview
              extensionDescription={extension.description}
              extensionName={extension.name}
              i18nDescription={descriptionText}
              i18nLastUpdate={lastUpdateText}
              i18nLastUpdateDate={lastUpdateDate}
              i18nName={nameText}
              i18nType={typeText}
              i18nTypeMessage={stepTypeMessage}
            />
          }
          supportsSection={
            <ExtensionSupports
              extensionActions={[
                { name: 'action-1', description: 'description-1' },
                { name: 'action-2', description: 'description-2' },
                { name: 'action-3', description: 'description-3' },
                { name: 'action-4', description: 'description-4' },
                { name: 'action-5', description: 'description-5' },
              ]}
            />
          }
        />
      </Router>
    ),
    { notes: inUseTestNotes }
  )
  .add(
    'connector extension not in use',
    () => (
      <Router>
        <ExtensionDetail
          extensionName={notUsedExtension.name}
          extensionUses={notUsedExtension.uses}
          i18nCancelText={'Cancel'}
          i18nDelete={deleteText}
          i18nDeleteModalMessage={
            'Are you sure you want to delete the extension?'
          }
          i18nDeleteModalTitle={'Confirm Delete?'}
          i18nDeleteTip={deleteTip}
          i18nIdMessage={idMsg}
          i18nOverviewSectionTitle={overviewText}
          i18nSupportsSectionTitle={supportedConnectorsText}
          i18nUpdate={updateText}
          i18nUsageSectionTitle={usageText}
          integrationsSection={<div />}
          linkUpdateExtension={'/extensions/'}
          onDelete={action(deleteActionText)}
          overviewSection={
            <ExtensionOverview
              extensionName={extension.name}
              i18nDescription={descriptionText}
              i18nLastUpdate={lastUpdateText}
              i18nLastUpdateDate={lastUpdateDate}
              i18nName={nameText}
              i18nType={typeText}
              i18nTypeMessage={connectorTypeMessage}
            />
          }
          supportsSection={
            <ExtensionSupports
              extensionActions={[
                { name: 'action-1', description: 'description-1' },
                { name: 'action-2', description: 'description-2' },
                { name: 'action-3', description: 'description-3' },
                { name: 'action-4', description: 'description-4' },
                { name: 'action-5', description: 'description-5' },
              ]}
            />
          }
        />
      </Router>
    ),
    { notes: notUsedTestNotes }
  )
  .add(
    'library extension in use',
    () => (
      <Router>
        <ExtensionDetail
          extensionName={libraryExtension.name}
          extensionUses={libraryExtension.uses}
          i18nCancelText={'Cancel'}
          i18nDelete={deleteText}
          i18nDeleteModalMessage={
            'Are you sure you want to delete the extension?'
          }
          i18nDeleteModalTitle={'Confirm Delete?'}
          i18nIdMessage={idMsg}
          i18nOverviewSectionTitle={overviewText}
          i18nSupportsSectionTitle={supportedLibrariesText}
          i18nUpdate={updateText}
          i18nUpdateTip={updateTip}
          i18nUsageSectionTitle={usageText}
          integrationsSection={<div />}
          linkUpdateExtension={'/extensions/'}
          onDelete={action(deleteActionText)}
          overviewSection={
            <ExtensionOverview
              extensionDescription={extension.description}
              extensionName={extension.name}
              i18nDescription={descriptionText}
              i18nLastUpdate={lastUpdateText}
              i18nLastUpdateDate={lastUpdateDate}
              i18nName={nameText}
              i18nType={typeText}
              i18nTypeMessage={libraryTypeMessage}
            />
          }
          supportsSection={
            <ExtensionSupports
              extensionActions={[
                { name: 'action-1', description: 'description-1' },
                { name: 'action-2', description: 'description-2' },
                { name: 'action-3', description: 'description-3' },
              ]}
            />
          }
        />
      </Router>
    ),
    { notes: libraryTypeTestNotes }
  );
