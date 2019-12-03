import { action } from '@storybook/addon-actions';
import { boolean, text } from '@storybook/addon-knobs';
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
const virtualizationName3 = 'Virtualization_3';
const virtualizationDescription3 = 'Virtualization 3 description ...';
const cancelText = 'Cancel';
const editText = 'Edit';
const viewOData = 'View OData';
const editTip1 = 'Edit ' + virtualizationName1 + ' virtualization';
const editTip2 = 'Edit ' + virtualizationName2 + ' virtualization';
const draftText = 'Draft';
const publishedText = 'Published';
const deleteText = 'Delete';
const errorText = 'Error';
const exportText = 'Export';
const unpublishText = 'Unpublish';
const publishText = 'Publish';
const confirmDeleteTitle = 'Delete Virtualization';
const confirmDeleteMessage =
  'This will permanently delete the Virtualization.  Proceed with the delete?';
const confirmUnpublishTitle = 'Unpublish Virtualization';
const confirmUnpublishMessage =
  'This Virtualization has been published.  Please unpublish the Virtualization first.';
const deleteInProgressText = 'Deleting...';
const publishInProgressText = 'Publishing...';
const stopInProgressText = 'Stopping...';
const publishLogUrlText = 'View Logs';
const currentStatusPublished = 'RUNNING';
const currentStatusDraft = 'NOTFOUND';
const currentStatusBuilding = 'BUILDING';

const viewItems = [
  <ViewListItem
    key="viewListItem1"
    viewId="viewListItem1"
    viewName={viewName1}
    viewDescription={viewDescription1}
    viewEditPageLink={''}
    i18nCancelText={'Cancel'}
    i18nDelete={deleteText}
    i18nDeleteTip={deleteText}
    i18nDeleteModalMessage={'Do you really want to delete the view?'}
    i18nDeleteModalTitle={'Confirm Delete'}
    i18nEdit={editText}
    i18nEditTip={editTip1}
    onDelete={action(deleteText)}
  />,
  <ViewListItem
    key="viewListItem2"
    viewId="viewListItem2"
    viewName={viewName2}
    viewDescription={viewDescription2}
    viewEditPageLink={''}
    i18nCancelText={'Cancel'}
    i18nDelete={deleteText}
    i18nDeleteTip={deleteText}
    i18nDeleteModalMessage={'Do you really want to delete the view?'}
    i18nDeleteModalTitle={'Confirm Delete'}
    i18nEdit={editText}
    i18nEditTip={editTip1}
    onDelete={action(deleteText)}
  />,
];

const virtItem = [
  <VirtualizationListItem
    key="virtualizationListItem1"
    isProgressWithLink={false}
    i18nDeleteInProgressText={deleteInProgressText}
    i18nPublishInProgressText={publishInProgressText}
    i18nStopInProgressText={stopInProgressText}
    i18nPublishState={draftText}
    labelType={'primary'}
    hasViews={true}
    virtualizationName={virtualizationName1}
    virtualizationDescription={virtualizationDescription1}
    detailsPageLink={''}
    i18nCancelText={cancelText}
    i18nDelete={deleteText}
    i18nDeleteModalMessage={confirmDeleteMessage}
    i18nDeleteModalTitle={confirmDeleteTitle}
    i18nEdit={editText}
    i18nViewODataUrlText={viewOData}
    i18nEditTip={editTip1}
    i18nExport={exportText}
    i18nInUseText={'Used by 1 integrations'}
    i18nStop={unpublishText}
    i18nPublish={publishText}
    i18nPublishLogUrlText={publishLogUrlText}
    i18nStopModalMessage={confirmUnpublishMessage}
    i18nStopModalTitle={confirmUnpublishTitle}
    modified={boolean('modified', false)}
    onDelete={action(deleteText)}
    onExport={action(exportText)}
    onStop={action(unpublishText)}
    onPublish={action(publishText)}
    currentPublishedState={currentStatusDraft}
    publishingLogUrl=""
    children={viewItems}
    usedBy={['MyVirt']}
  />,
];

const virtualizationItems = [
  <VirtualizationListItem
    key="virtualizationListItem1"
    isProgressWithLink={false}
    i18nDeleteInProgressText={deleteInProgressText}
    i18nPublishInProgressText={publishInProgressText}
    i18nStopInProgressText={stopInProgressText}
    i18nPublishState={draftText}
    labelType={'default'}
    hasViews={true}
    virtualizationName={virtualizationName1}
    virtualizationDescription={virtualizationDescription1}
    detailsPageLink={''}
    i18nCancelText={cancelText}
    i18nDelete={deleteText}
    i18nDeleteModalMessage={confirmDeleteMessage}
    i18nDeleteModalTitle={confirmDeleteTitle}
    i18nEdit={editText}
    i18nViewODataUrlText={viewOData}
    i18nEditTip={editTip1}
    i18nExport={exportText}
    i18nInUseText={'Used by 0 integrations'}
    i18nStop={unpublishText}
    i18nPublish={publishText}
    i18nPublishLogUrlText={publishLogUrlText}
    i18nStopModalMessage={confirmUnpublishMessage}
    i18nStopModalTitle={confirmUnpublishTitle}
    modified={boolean('modified', false)}
    onDelete={action(deleteText)}
    onExport={action(exportText)}
    onStop={action(unpublishText)}
    onPublish={action(publishText)}
    currentPublishedState={currentStatusDraft}
    publishingLogUrl=""
    usedBy={[]}
  />,
  <VirtualizationListItem
    key="virtualizationListItem2"
    isProgressWithLink={false}
    i18nDeleteInProgressText={deleteInProgressText}
    i18nPublishInProgressText={publishInProgressText}
    i18nStopInProgressText={stopInProgressText}
    i18nPublishState={publishInProgressText}
    labelType={'default'}
    hasViews={true}
    virtualizationName={virtualizationName2}
    virtualizationDescription={virtualizationDescription2}
    detailsPageLink={''}
    i18nCancelText={cancelText}
    i18nDelete={deleteText}
    i18nDeleteModalMessage={confirmDeleteMessage}
    i18nDeleteModalTitle={confirmDeleteTitle}
    i18nEdit={editText}
    i18nViewODataUrlText={viewOData}
    i18nEditTip={editTip2}
    i18nExport={exportText}
    i18nInUseText={'Used by 0 integrations'}
    i18nStop={unpublishText}
    i18nPublish={publishText}
    i18nPublishLogUrlText={publishLogUrlText}
    i18nStopModalMessage={confirmUnpublishMessage}
    i18nStopModalTitle={confirmUnpublishTitle}
    modified={boolean('modified', false)}
    onDelete={action(deleteText)}
    onExport={action(exportText)}
    onStop={action(unpublishText)}
    onPublish={action(publishText)}
    currentPublishedState={currentStatusPublished}
    usedBy={[]}
  />,
  <VirtualizationListItem
    key="virtualizationListItem3"
    isProgressWithLink={true}
    i18nDeleteInProgressText={deleteInProgressText}
    i18nPublishInProgressText={publishInProgressText}
    i18nStopInProgressText={stopInProgressText}
    i18nPublishState={publishedText}
    labelType={'primary'}
    hasViews={true}
    virtualizationName={virtualizationName3}
    virtualizationDescription={virtualizationDescription3}
    detailsPageLink={''}
    i18nCancelText={cancelText}
    i18nDelete={deleteText}
    i18nDeleteModalMessage={confirmDeleteMessage}
    i18nDeleteModalTitle={confirmDeleteTitle}
    i18nEdit={editText}
    i18nViewODataUrlText={viewOData}
    i18nEditTip={editTip2}
    i18nExport={exportText}
    i18nInUseText={'Used by 0 integrations'}
    i18nStop={unpublishText}
    i18nPublish={publishText}
    i18nPublishLogUrlText={publishLogUrlText}
    i18nStopModalMessage={confirmUnpublishMessage}
    i18nStopModalTitle={confirmUnpublishTitle}
    modified={boolean('modified', false)}
    onDelete={action(deleteText)}
    onExport={action(exportText)}
    onStop={action(unpublishText)}
    onPublish={action(publishText)}
    currentPublishedState={currentStatusBuilding}
    publishingLogUrl={'log/usl/goes/here'}
    publishingCurrentStep={2}
    publishingTotalSteps={4}
    publishingStepText={'Building'}
    usedBy={[]}
  />,
];

const createVirtualization = 'Create Data Virtualization';
const createVirtualizationTip = 'Create Data Virtualization';
const importText = 'Import';
const importTip = 'Import a data virtualization';

const defaultNotes =
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

const threeVirtualizationsTestNotes =
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
          currentSortType={{
            id: 'sort',
            isNumeric: false,
            title: 'Sort',
          }}
          currentValue={''}
          filterTypes={[]}
          isSortAscending={true}
          linkCreateHRef={action('/data/create')}
          linkImportHRef={action('/data/import')}
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
          /* TD-636: Commented out for TP 
          onImport={action(importText)} */
          children={[]}
          hasListData={false}
        />
      </Router>
    ))
  )

  .add(
    '3 virtualizations',
    withNotes(threeVirtualizationsTestNotes)(() => (
      <Router>
        <VirtualizationList
          activeFilters={[]}
          currentFilterType={{
            filterType: 'text',
            id: 'name',
            placeholder: text('placeholder', 'Filter by name'),
            title: text('title', 'Name'),
          }}
          currentSortType={{
            id: 'sort',
            isNumeric: false,
            title: 'Sort',
          }}
          currentValue={''}
          filterTypes={[]}
          isSortAscending={true}
          linkCreateHRef={action('/data/create')}
          linkImportHRef={action('/data/import')}
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
          /* TD-636: Commented out for TP 
          onImport={action(importText)} */
          children={virtualizationItems}
          hasListData={true}
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
          currentSortType={{
            id: 'sort',
            isNumeric: false,
            title: 'Sort',
          }}
          currentValue={''}
          filterTypes={[]}
          isSortAscending={true}
          linkCreateHRef={action('/data/create')}
          linkImportHRef={action('/data/import')}
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
          /* TD-636: Commented out for TP 
          onImport={action(importText)} */
          children={virtItem}
          hasListData={true}
        />
      </Router>
    ))
  );
