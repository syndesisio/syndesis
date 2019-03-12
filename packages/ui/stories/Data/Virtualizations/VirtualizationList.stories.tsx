import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';

import {
  ViewListItem,
  VirtualizationList,
  VirtualizationListItem,
} from '../../../src';

const stories = storiesOf('Data/Virtualizations/VirtualizationList', module);

const viewName1 = 'View_1';
const viewDescription1 = 'View 1 description ...';
const viewName2 = 'View_2';
const viewDescription2 = 'View 2 description ...';

const virtualizationName1 = 'Virtualization_1';
const virtualizationDescription1 = 'Virtualization 1 description ...';
const virtualizationName2 = 'Virtualization_2';
const virtualizationDescription2 = 'Virtualization 2 description ...';
const cancelText = 'Cancel';
const editText = 'Edit';
const editTip1 = 'Edit ' + virtualizationName1 + ' virtualization';
const editTip2 = 'Edit ' + virtualizationName2 + ' virtualization';
const draftText = 'Draft';
const publishedText = 'Published';
const deleteText = 'Delete';
const errorText = 'Error';
const exportText = 'Export';
const unpublishText = 'Unpublish';
const publishText = 'Publish';
const deleteModalTitle = 'Confirm Delete?';
const deleteModalMessage =
  'Are you sure you want to delete the virtualization?';
const publishInProgressText = 'Publish In Progress';
const publishLogUrlText = 'View Logs';
const currentStatusPublished = 'RUNNING';
const currentStatusDraft = 'NOTFOUND';

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
  <VirtualizationListItem
    key="virtualizationListItem1"
    virtualizationName={virtualizationName1}
    virtualizationDescription={virtualizationDescription1}
    i18nCancelText={cancelText}
    i18nDelete={deleteText}
    i18nDeleteModalMessage={deleteModalMessage}
    i18nDeleteModalTitle={deleteModalTitle}
    i18nDraft={draftText}
    i18nEdit={editText}
    i18nEditTip={editTip1}
    i18nError={errorText}
    i18nExport={exportText}
    i18nPublished={publishedText}
    i18nUnpublish={unpublishText}
    i18nPublish={publishText}
    i18nPublishInProgress={publishInProgressText}
    i18nPublishLogUrlText={publishLogUrlText}
    onDelete={action(deleteText)}
    onEdit={action(editText)}
    onExport={action(exportText)}
    onUnpublish={action(unpublishText)}
    onPublish={action(publishText)}
    currentPublishedState={currentStatusDraft}
    publishLogUrl=""
    children={viewItems}
  />,
];

const virtualizationItems = [
  <VirtualizationListItem
    key="virtualizationListItem1"
    virtualizationName={virtualizationName1}
    virtualizationDescription={virtualizationDescription1}
    i18nCancelText={cancelText}
    i18nDelete={deleteText}
    i18nDeleteModalMessage={deleteModalMessage}
    i18nDeleteModalTitle={deleteModalTitle}
    i18nDraft={draftText}
    i18nEdit={editText}
    i18nEditTip={editTip1}
    i18nError={errorText}
    i18nExport={exportText}
    i18nPublished={publishedText}
    i18nUnpublish={unpublishText}
    i18nPublish={publishText}
    i18nPublishInProgress={publishInProgressText}
    i18nPublishLogUrlText={publishLogUrlText}
    onDelete={action(deleteText)}
    onEdit={action(editText)}
    onExport={action(exportText)}
    onUnpublish={action(unpublishText)}
    onPublish={action(publishText)}
    currentPublishedState={currentStatusDraft}
    publishLogUrl=""
  />,
  <VirtualizationListItem
    key="virtualizationListItem2"
    virtualizationName={virtualizationName2}
    virtualizationDescription={virtualizationDescription2}
    i18nCancelText={cancelText}
    i18nDelete={deleteText}
    i18nDeleteModalMessage={deleteModalMessage}
    i18nDeleteModalTitle={deleteModalTitle}
    i18nDraft={draftText}
    i18nEdit={editText}
    i18nEditTip={editTip2}
    i18nError={errorText}
    i18nExport={exportText}
    i18nPublished={publishedText}
    i18nUnpublish={unpublishText}
    i18nPublish={publishText}
    i18nPublishInProgress={publishInProgressText}
    i18nPublishLogUrlText={publishLogUrlText}
    onDelete={action(deleteText)}
    onEdit={action(editText)}
    onExport={action(exportText)}
    onUnpublish={action(unpublishText)}
    onPublish={action(publishText)}
    currentPublishedState={currentStatusPublished}
    publishLogUrl=""
  />,
];

const title = 'Virtualizations';
const description =
  'Syndesis creates and manages data virtualizations to expose as data source connections.';
const createVirtualization = 'Create Data Virtualization';
const createVirtualizationTip = 'Create Data Virtualization';
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
  createVirtualization +
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
  createVirtualization +
  '" button is enabled\n' +
  '- Verify toolbar "' +
  createVirtualization +
  '" button tooltip is "' +
  createVirtualizationTip;

const twoVirtualizationsTestNotes =
  defaultNotes +
  '"\n' +
  '- Verify empty state component does not show\n' +
  '- Verify results message shows ' +
  virtualizationItems.length +
  ' results\n' +
  '- Verify ' +
  virtualizationItems.length +
  ' Virtualization list items are displayed\n' +
  '- Verify first virtualization is "' +
  publishedText +
  '"\n' +
  '- Verify second virtualization is in "' +
  draftText +
  '" mode';

const noVirtualizationsTestNotes =
  defaultNotes +
  '"\n' +
  '- Verify results message shows 0 results\n' +
  '- Verify empty state component is displayed and has a New Virtualization button\n' +
  '- Verify no virtualization items are displayed';

const singleVirtualizationWithViewsTestNotes =
  defaultNotes +
  '"\n' +
  '- Verify results message shows 1 results\n' +
  '- Verify 1 virtualization is displayed and is expandable\n' +
  '- Verify expanding virtualization shows 2 views\n';

stories

  .add(
    'empty list',
    withNotes(noVirtualizationsTestNotes)(() => (
      <Router>
        <VirtualizationList
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
          i18nCreateDataVirtualization={createVirtualization}
          i18nCreateDataVirtualizationTip={createVirtualization}
          i18nDescription={text('i18nDescription', description)}
          i18nEmptyStateInfo={text(
            'i18nEmptyStateInfo',
            'There are no currently available Virtualizations. Please click on the button below to create one.'
          )}
          i18nEmptyStateTitle={text(
            'i18nEmptyStateTitle',
            createVirtualization
          )}
          i18nImport={importText}
          i18nImportTip={importTip}
          i18nLinkCreateVirtualization={text(
            'i18nLinkCreateVirtualization',
            createVirtualization
          )}
          i18nLinkCreateVirtualizationTip={createVirtualizationTip}
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
    withNotes(twoVirtualizationsTestNotes)(() => (
      <Router>
        <VirtualizationList
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
          i18nCreateDataVirtualization={createVirtualization}
          i18nCreateDataVirtualizationTip={createVirtualization}
          i18nDescription={text('i18nDescription', description)}
          i18nEmptyStateInfo={text(
            'i18nEmptyStateInfo',
            'There are no currently available API connectors. Please click on the button below to create one.'
          )}
          i18nEmptyStateTitle={text(
            'i18nEmptyStateTitle',
            createVirtualization
          )}
          i18nImport={importText}
          i18nImportTip={importTip}
          i18nLinkCreateVirtualization={text(
            'i18nLinkCreateVirtualization',
            createVirtualization
          )}
          i18nName={text('i18nName', 'Name')}
          i18nNameFilterPlaceholder={text(
            'i18nNameFilterPlaceholder',
            'Filter by Name...'
          )}
          i18nResultsCount={text(
            'i18nResultsCount',
            virtualizationItems.length + ' Results'
          )}
          i18nTitle={text('i18nTitle', title)}
          onImport={action(importText)}
          children={virtualizationItems}
        />
      </Router>
    ))
  )

  .add(
    'single virtualization - expand views',
    withNotes(singleVirtualizationWithViewsTestNotes)(() => (
      <Router>
        <VirtualizationList
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
          i18nCreateDataVirtualization={createVirtualization}
          i18nCreateDataVirtualizationTip={createVirtualization}
          i18nDescription={text('i18nDescription', description)}
          i18nEmptyStateInfo={text(
            'i18nEmptyStateInfo',
            'There are no currently available API connectors. Please click on the button below to create one.'
          )}
          i18nEmptyStateTitle={text(
            'i18nEmptyStateTitle',
            createVirtualization
          )}
          i18nImport={importText}
          i18nImportTip={importTip}
          i18nLinkCreateVirtualization={text(
            'i18nLinkCreateVirtualization',
            createVirtualization
          )}
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
          onImport={action(importText)}
          children={virtItem}
        />
      </Router>
    ))
  );
