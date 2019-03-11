import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';

import { ViewListItem, ViewListView } from '../../../src';

const stories = storiesOf('Data/Views/ViewListView', module);

const viewName1 = 'CustomerInfo';
const viewDescription1 = 'Description for CustomerInfo';
const viewName2 = 'AccountsSummary';
const viewDescription2 = 'Description for AccountsSummary';
const editText = 'Edit';
const editTip1 = 'Edit ' + viewName1 + ' view';
const editTip2 = 'Edit ' + viewName2 + ' view';
const editActionText = 'Edit View';
const deleteText = 'Delete';
const deleteTip1 = 'Delete ' + viewName1 + ' view';
const deleteTip2 = 'Delete ' + viewName2 + ' view';
const deleteActionText = 'Delete View';

const viewItems = [
  <ViewListItem
    key="viewListItem1"
    viewName={text('name', viewName1)}
    viewDescription={text('description', viewDescription1)}
    i18nDelete={text('i18nDelete', deleteText)}
    i18nDeleteTip={text('i18nDeleteTip1', deleteTip1)}
    i18nEdit={text('i18nEdit', editText)}
    i18nEditTip={text('i18nEditTip1', editTip1)}
    onDelete={action(deleteActionText)}
    onEdit={action(editActionText)}
  />,
  <ViewListItem
    key="viewListItem2"
    viewName={text('name', viewName2)}
    viewDescription={text('description', viewDescription2)}
    i18nDelete={text('i18nDelete', deleteText)}
    i18nDeleteTip={text('i18nDeleteTip2', deleteTip2)}
    i18nEdit={text('i18nEdit', editText)}
    i18nEditTip={text('i18nEditTip2', editTip2)}
    onDelete={action(deleteActionText)}
    onEdit={action(editActionText)}
  />,
];

const createView = 'Create View';
const createViewTip = 'Create a new view';
const importView = 'Import';
const importViewTip = 'Import a view';
const importActionText = 'Import View';

const hasViewsTestNotes =
  '- Verify toolbar is displayed\n' +
  '- Verify toolbar contains "' +
  importView +
  '" button\n' +
  '- Verify toolbar "' +
  importView +
  '" button is enabled\n' +
  '- Verify toolbar "' +
  importView +
  '" button tooltip is "' +
  importViewTip +
  '"\n' +
  '- Verify toolbar contains "' +
  createView +
  '" button\n' +
  '- Verify toolbar "' +
  createView +
  '" button is enabled\n' +
  '- Verify toolbar "' +
  createView +
  '" button tooltip is "' +
  createView +
  '"\n' +
  '- Verify empty state component does not show\n' +
  '- Verify results message shows ' +
  viewItems.length +
  ' results\n' +
  '- Verify ' +
  viewItems.length +
  ' View list items are displayed\n' +
  '- Verify clicking the Edit button prints "' +
  editActionText +
  '" in the ACTION LOGGER\n' +
  '- Verify selecting Delete in the kebab menu prints "' +
  deleteActionText +
  '" in the ACTION LOGGER';

const noViewsTestNotes =
  '- Verify toolbar is displayed\n' +
  '- Verify toolbar contains "' +
  importView +
  '" button\n' +
  '- Verify toolbar "' +
  importView +
  '" button is enabled\n' +
  '- Verify toolbar "' +
  importView +
  '" button tooltip is "' +
  importViewTip +
  '"\n' +
  '- Verify toolbar contains "' +
  createView +
  '" button\n' +
  '- Verify toolbar "' +
  createView +
  '" button is enabled\n' +
  '- Verify toolbar "' +
  createView +
  '" button tooltip is "' +
  createViewTip +
  '"\n' +
  '- Verify results message shows 0 results\n' +
  '- Verify empty state component is displayed and has a Create View button\n' +
  '- Verify no view items are displayed';

stories

  .add(
    'no Views',
    withNotes(noViewsTestNotes)(() => (
      <Router>
        <ViewListView
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
          linkCreateView={action('/data/create')}
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
          i18nEmptyStateInfo={text(
            'i18nEmptyStateInfo',
            'There are no currently available Views. Please click on the button below to create one.'
          )}
          i18nEmptyStateTitle={text('i18nEmptyStateTitle', createView)}
          i18nImportView={importView}
          i18nImportViewTip={importViewTip}
          i18nLinkCreateView={text('i18nLinkCreateView', createView)}
          i18nLinkCreateViewTip={createViewTip}
          i18nName={text('i18nName', 'Name')}
          i18nNameFilterPlaceholder={text(
            'i18nNameFilterPlaceholder',
            'Filter by Name...'
          )}
          i18nResultsCount={text('i18nResultsCount', '0 Results')}
          onImportView={action(importActionText)}
          children={[]}
        />
      </Router>
    ))
  )

  .add(
    'has Views',
    withNotes(hasViewsTestNotes)(() => (
      <Router>
        <ViewListView
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
          linkCreateView={action('/data/create')}
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
          i18nEmptyStateInfo={text(
            'i18nEmptyStateInfo',
            'There are no currently available API connectors. Please click on the button below to create one.'
          )}
          i18nEmptyStateTitle={text('i18nEmptyStateTitle', createView)}
          i18nImportView={importView}
          i18nImportViewTip={importViewTip}
          i18nLinkCreateView={text('i18nLinkCreateView', createView)}
          i18nName={text('i18nName', 'Name')}
          i18nNameFilterPlaceholder={text(
            'i18nNameFilterPlaceholder',
            'Filter by Name...'
          )}
          i18nResultsCount={text(
            'i18nResultsCount',
            viewItems.length + ' Results'
          )}
          onImportView={action(importActionText)}
          children={viewItems}
        />
      </Router>
    ))
  );
