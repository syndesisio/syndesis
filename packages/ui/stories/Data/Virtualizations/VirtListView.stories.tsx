import { action } from '@storybook/addon-actions';
import { boolean } from '@storybook/addon-knobs';
import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';

import { ViewListItem, VirtListItem, VirtListView } from '../../../src';

const stories = storiesOf('Data/Virtualizations/VirtListView', module);

const viewName1 = 'View_1';
const viewDescription1 = 'View 1 description ...';
const viewName2 = 'View_2';
const viewDescription2 = 'View 2 description ...';

const virtName1 = 'Virtualization_1';
const virtDescription1 = 'Virtualization 1 description ...';
const virtName2 = 'Virtualization_2';
const virtDescription2 = 'Virtualization 2 description ...';
const cancelText = 'Cancel';
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
const deleteModalTitle = 'Confirm Delete?';
const deleteModalMessage =
  'Are you sure you want to delete the virtualization?';

const viewItems = [
  <ViewListItem
    key="viewListItem1"
    viewName={viewName1}
    viewDescription={viewDescription1}
    i18nDelete={deleteText}
    i18nDeleteTip={deleteText}
    i18nEdit={editText}
    i18nEditTip={editTip1}
    onDelete={action(deleteText)}
    onEdit={action(editText)}
  />,
  <ViewListItem
    key="viewListItem2"
    viewName={viewName2}
    viewDescription={viewDescription2}
    i18nDelete={deleteText}
    i18nDeleteTip={deleteText}
    i18nEdit={editText}
    i18nEditTip={editTip1}
    onDelete={action(deleteText)}
    onEdit={action(editText)}
  />,
];

const virtItem = [
  <VirtListItem
    key="virtListItem1"
    virtName={virtName1}
    virtDescription={virtDescription1}
    i18nCancelText={cancelText}
    i18nDelete={deleteText}
    i18nDeleteModalMessage={deleteModalMessage}
    i18nDeleteModalTitle={deleteModalTitle}
    i18nDraft={draftText}
    i18nDraftTip={draftTip1}
    i18nEdit={editText}
    i18nEditTip={editTip1}
    i18nExport={exportText}
    i18nPublished={publishedText}
    i18nPublishedTip={publishedTip1}
    i18nUnpublish={unpublishText}
    i18nPublish={publishText}
    onDelete={action(deleteText)}
    onEdit={action(editText)}
    onExport={action(exportText)}
    onUnpublish={action(unpublishText)}
    onPublish={action(publishText)}
    isPublished={boolean(publishText, true)}
    children={viewItems}
  />,
];

const virtItems = [
  <VirtListItem
    key="virtListItem1"
    virtName={virtName1}
    virtDescription={virtDescription1}
    i18nCancelText={cancelText}
    i18nDelete={deleteText}
    i18nDeleteModalMessage={deleteModalMessage}
    i18nDeleteModalTitle={deleteModalTitle}
    i18nDraft={draftText}
    i18nDraftTip={draftTip1}
    i18nEdit={editText}
    i18nEditTip={editTip1}
    i18nExport={exportText}
    i18nPublished={publishedText}
    i18nPublishedTip={publishedTip1}
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
    key="virtListItem2"
    virtName={virtName2}
    virtDescription={virtDescription2}
    i18nCancelText={cancelText}
    i18nDelete={deleteText}
    i18nDeleteModalMessage={deleteModalMessage}
    i18nDeleteModalTitle={deleteModalTitle}
    i18nDraft={draftText}
    i18nDraftTip={draftTip2}
    i18nEdit={editText}
    i18nEditTip={editTip2}
    i18nExport={exportText}
    i18nPublished={publishedText}
    i18nPublishedTip={publishedTip2}
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

const defaultNotes =
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
  createVirtTip;

const twoVirtsTestNotes =
  defaultNotes +
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
  defaultNotes +
  '"\n' +
  '- Verify results message shows 0 results\n' +
  '- Verify empty state component is displayed and has a New Virtualization button\n' +
  '- Verify no virtualization items are displayed';

const singleVirtWithViewsTestNotes =
  defaultNotes +
  '"\n' +
  '- Verify results message shows 1 results\n' +
  '- Verify 1 virtualization is displayed and is expandable\n' +
  '- Verify expanding virtualization shows 2 views\n';

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
          onImport={action(importText)}
          children={[]}
        />
      </Router>
    ))
  )

  .add(
    '2 virtualizations',
    withNotes(twoVirtsTestNotes)(() => (
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
          onImport={action(importText)}
          children={virtItems}
        />
      </Router>
    ))
  )

  .add(
    'single virtualization - expand views',
    withNotes(singleVirtWithViewsTestNotes)(() => (
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
            virtItem.length + ' Results'
          )}
          i18nTitle={text('i18nTitle', title)}
          onCreate={action(createVirt)}
          onImport={action(importText)}
          children={virtItem}
        />
      </Router>
    ))
  );
