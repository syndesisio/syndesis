import { action } from '@storybook/addon-actions';
import { boolean } from '@storybook/addon-knobs';
import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';

import { VirtListItem, VirtListView } from '../../../src';

const stories = storiesOf('Data/Virtualizations/VirtListView', module);

const virtName1 = 'Virtualization_1';
const virtDescription1 = 'Virtualization 1 description ...';
const virtName2 = 'Virtualization_2';
const virtDescription2 = 'Virtualization 2 description ...';
const editText = 'Edit';
const editTip1 = 'Edit ' + virtName1 + ' virtualization';
const editTip2 = 'Edit ' + virtName2 + ' virtualization';
const draftText = 'Draft';
const draftTip1 = 'The virtualization ' + virtName1 + ' has not been published';
const draftTip2 = 'The virtualization ' + virtName2 + ' has not been published';
const publishedText = 'Published';
const publishedTip1 = 'The virtualization ' + virtName1 + ' is published';
const publishedTip2 = 'The virtualization ' + virtName2 + ' is published';
const deleteText = 'Delete';
const exportText = 'Export';
const unpublishText = 'Unpublish';
const publishText = 'Publish';

const virtItems = [
  <VirtListItem
    key="viewListItem1"
    virtName={virtName1}
    virtDescription={virtDescription1}
    i18nDraft={draftText}
    i18nDraftTip={draftTip1}
    i18nEdit={editText}
    i18nEditTip={editTip1}
    i18nPublished={publishedText}
    i18nPublishedTip={publishedTip1}
    i18nDelete={deleteText}
    i18nExport={exportText}
    i18nUnpublish={unpublishText}
    i18nPublish={publishText}
    onDelete={action(deleteText)}
    onEdit={action(editText)}
    onExport={action(exportText)}
    onUnpublish={action(unpublishText)}
    onPublish={action(publishText)}
    isPublished={boolean(publishText, true)}
  />,
  <VirtListItem
    key="viewListItem2"
    virtName={virtName2}
    virtDescription={virtDescription2}
    i18nDraft={draftText}
    i18nDraftTip={draftTip2}
    i18nEdit={editText}
    i18nEditTip={editTip2}
    i18nPublished={publishedText}
    i18nPublishedTip={publishedTip2}
    i18nDelete={deleteText}
    i18nExport={exportText}
    i18nUnpublish={unpublishText}
    i18nPublish={publishText}
    onDelete={action(deleteText)}
    onEdit={action(editText)}
    onExport={action(exportText)}
    onUnpublish={action(unpublishText)}
    onPublish={action(publishText)}
    isPublished={boolean(publishText, false)}
  />,
];

const title = 'Virtualizations';
const description =
  'Syndesis creates and manages data virtualizations to expose as data source connections.';
const createVirt = 'Create Data Virtualization';
const createVirtTip = 'Create Data Virtualization';
const importText = 'Import';
const importTip = 'Import a data virtualization';

const hasVirtsTestNotes =
  '- Verify page title is "' +
  title +
  '"\n' +
  '- Verify page description is "' +
  description +
  '"\n' +
  '- Verify toolbar is displayed\n' +
  '- Verify toolbar contains "' +
  importText +
  ' and ' +
  createVirt +
  '" buttons\n' +
  '- Verify toolbar "' +
  importText +
  '" button is enabled\n' +
  '- Verify toolbar "' +
  importText +
  '" button tooltip is "' +
  importTip +
  '"\n' +
  '- Verify toolbar "' +
  createVirt +
  '" button is enabled\n' +
  '- Verify toolbar "' +
  createVirt +
  '" button tooltip is "' +
  createVirtTip +
  '"\n' +
  '- Verify empty state component does not show\n' +
  '- Verify results message shows ' +
  virtItems.length +
  ' results\n' +
  '- Verify ' +
  virtItems.length +
  ' Virtualization list items are displayed\n' +
  '- Verify first virtualization is "' +
  publishedText +
  '"\n' +
  '- Verify second virtualization is in "' +
  draftText +
  '" mode';

const noVirtsTestNotes =
  '- Verify page title is "' +
  title +
  '"\n' +
  '- Verify page description is "' +
  description +
  '"\n' +
  '- Verify toolbar is displayed\n' +
  '- Verify toolbar contains "' +
  importText +
  ' and ' +
  createVirt +
  '" buttons\n' +
  '- Verify toolbar "' +
  importText +
  '" button is enabled\n' +
  '- Verify toolbar "' +
  importText +
  '" button tooltip is "' +
  importTip +
  '"\n' +
  '- Verify toolbar "' +
  createVirt +
  '" button is enabled\n' +
  '- Verify toolbar "' +
  createVirt +
  '" button tooltip is "' +
  createVirtTip +
  '"\n' +
  '- Verify results message shows 0 results\n' +
  '- Verify empty state component is displayed and has a New Virtualization button\n' +
  '- Verify no virtualization items are displayed';

stories

  .add(
    'empty list',
    withNotes(noVirtsTestNotes)(() => (
      <Router>
        <VirtListView
          activeFilters={[]}
          currentFilterType={{
            filterType: 'text',
            id: 'name',
            placeholder: text('placeholder', 'Filter by name'),
            title: text('title', 'Name'),
          }}
          currentSortType={'sort'}
          currentValue={''}
          filterTypes={[]}
          isSortAscending={true}
          linkCreateHRef={action('/data/create')}
          resultsCount={0}
          sortTypes={[]}
          onUpdateCurrentValue={action('onUpdateCurrentValue')}
          onValueKeyPress={action('onValueKeyPress')}
          onFilterAdded={action('onFilterAdded')}
          onSelectFilterType={action('onSelectFilterType')}
          onFilterValueSelected={action('onFilterValueSelected')}
          onRemoveFilter={action('onRemoveFilter')}
          onClearFilters={action('onClearFilters')}
          onToggleCurrentSortDirection={action('onToggleCurrentSortDirection')}
          onUpdateCurrentSortType={action('onUpdateCurrentSortType')}
          i18nCreateDataVirt={createVirt}
          i18nCreateDataVirtTip={createVirt}
          i18nDescription={text('i18nDescription', description)}
          i18nEmptyStateInfo={text(
            'i18nEmptyStateInfo',
            'There are no currently available Virts. Please click on the button below to create one.'
          )}
          i18nEmptyStateTitle={text('i18nEmptyStateTitle', createVirt)}
          i18nImport={importText}
          i18nImportTip={importTip}
          i18nLinkCreateVirt={text('i18nLinkCreateVirt', createVirt)}
          i18nLinkCreateVirtTip={createVirtTip}
          i18nName={text('i18nName', 'Name')}
          i18nNameFilterPlaceholder={text(
            'i18nNameFilterPlaceholder',
            'Filter by Name...'
          )}
          i18nResultsCount={text('i18nResultsCount', '0 Results')}
          i18nTitle={text('i18nTitle', title)}
          onCreate={action(createVirt)}
          onImport={action(importText)}
          children={[]}
        />
      </Router>
    ))
  )

  .add(
    'has virtualizations',
    withNotes(hasVirtsTestNotes)(() => (
      <Router>
        <VirtListView
          activeFilters={[]}
          currentFilterType={{
            filterType: 'text',
            id: 'name',
            placeholder: text('placeholder', 'Filter by name'),
            title: text('title', 'Name'),
          }}
          currentSortType={'sort'}
          currentValue={''}
          filterTypes={[]}
          isSortAscending={true}
          linkCreateHRef={action('/data/create')}
          resultsCount={0}
          sortTypes={[]}
          onUpdateCurrentValue={action('onUpdateCurrentValue')}
          onValueKeyPress={action('onValueKeyPress')}
          onFilterAdded={action('onFilterAdded')}
          onSelectFilterType={action('onSelectFilterType')}
          onFilterValueSelected={action('onFilterValueSelected')}
          onRemoveFilter={action('onRemoveFilter')}
          onClearFilters={action('onClearFilters')}
          onToggleCurrentSortDirection={action('onToggleCurrentSortDirection')}
          onUpdateCurrentSortType={action('onUpdateCurrentSortType')}
          i18nCreateDataVirt={createVirt}
          i18nCreateDataVirtTip={createVirt}
          i18nDescription={text('i18nDescription', description)}
          i18nEmptyStateInfo={text(
            'i18nEmptyStateInfo',
            'There are no currently available API connectors. Please click on the button below to create one.'
          )}
          i18nEmptyStateTitle={text('i18nEmptyStateTitle', createVirt)}
          i18nImport={importText}
          i18nImportTip={importTip}
          i18nLinkCreateVirt={text('i18nLinkCreateVirt', createVirt)}
          i18nName={text('i18nName', 'Name')}
          i18nNameFilterPlaceholder={text(
            'i18nNameFilterPlaceholder',
            'Filter by Name...'
          )}
          i18nResultsCount={text(
            'i18nResultsCount',
            virtItems.length + ' Results'
          )}
          i18nTitle={text('i18nTitle', title)}
          onCreate={action(createVirt)}
          onImport={action(importText)}
          children={virtItems}
        />
      </Router>
    ))
  );
