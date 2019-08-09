import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';

import { ViewList, ViewListItem } from '../../../src';

const stories = storiesOf('Data/Views/ViewList', module);

const viewName1 = 'CustomerInfo';
const viewDescription1 = 'Description for CustomerInfo';
const viewName2 = 'AccountsSummary';
const viewDescription2 = 'Description for AccountsSummary';
const editText = 'Edit';
const editTip1 = 'Edit ' + viewName1 + ' view';
const editTip2 = 'Edit ' + viewName2 + ' view';
const deleteText = 'Delete';
const deleteTip1 = 'Delete ' + viewName1 + ' view';
const deleteTip2 = 'Delete ' + viewName2 + ' view';
const deleteActionText = 'Delete View';

const viewItems = [
  <ViewListItem
    key="viewListItem1"
    viewId="viewListItem1"
    viewName={text('name', viewName1)}
    viewDescription={text('description', viewDescription1)}
    viewEditPageLink={''}
    i18nCancelText={'Cancel'}
    i18nDelete={text('i18nDelete', deleteText)}
    i18nDeleteTip={text('i18nDeleteTip1', deleteTip1)}
    i18nDeleteModalMessage={'Do you really want to delete the view?'}
    i18nDeleteModalTitle={'Confirm Delete'}
    i18nEdit={text('i18nEdit', editText)}
    i18nEditTip={text('i18nEditTip1', editTip1)}
    onDelete={action(deleteActionText)}
  />,
  <ViewListItem
    key="viewListItem2"
    viewId="viewListItem2"
    viewName={text('name', viewName2)}
    viewEditPageLink={''}
    i18nCancelText={'Cancel'}
    viewDescription={text('description', viewDescription2)}
    i18nDelete={text('i18nDelete', deleteText)}
    i18nDeleteTip={text('i18nDeleteTip2', deleteTip2)}
    i18nDeleteModalMessage={'Do you really want to delete the view?'}
    i18nDeleteModalTitle={'Confirm Delete'}
    i18nEdit={text('i18nEdit', editText)}
    i18nEditTip={text('i18nEditTip2', editTip2)}
    onDelete={action(deleteActionText)}
  />,
];

const createView = 'Create View';
const createViewTip = 'Create a new view';
const importViews = 'Import Data Source';
const importViewsTip = 'Import data source';

const hasViewsTestNotes =
  '- Verify toolbar is displayed\n' +
  '- Verify toolbar contains "' +
  importViews +
  '" button\n' +
  '- Verify toolbar "' +
  importViews +
  '" button is enabled\n' +
  '- Verify toolbar "' +
  importViews +
  '" button tooltip is "' +
  importViewsTip +
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
  '- Verify selecting Delete in the kebab menu prints "' +
  deleteActionText +
  '" in the ACTION LOGGER';

const noViewsTestNotes =
  '- Verify toolbar is displayed\n' +
  '- Verify toolbar contains "' +
  importViews +
  '" button\n' +
  '- Verify toolbar "' +
  importViews +
  '" button is enabled\n' +
  '- Verify toolbar "' +
  importViews +
  '" button tooltip is "' +
  importViewsTip +
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
        <ViewList
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
          linkCreateViewHRef={action('/data/create')}
          linkImportViewsHRef={action('/data/import')}
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
          i18nCreateView={text('i18nLinkCreateView', createView)}
          i18nImportViews={importViews}
          i18nImportViewsTip={importViewsTip}
          i18nDescription={text('i18nDescription', 'Name')}
          i18nName={text('i18nName', 'Name')}
          i18nNameFilterPlaceholder={text(
            'i18nNameFilterPlaceholder',
            'Filter by Name...'
          )}
          i18nResultsCount={text('i18nResultsCount', '0 Results')}
          children={[]}
          hasListData={false}
        />
      </Router>
    ))
  )

  .add(
    'has Views',
    withNotes(hasViewsTestNotes)(() => (
      <Router>
        <ViewList
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
          linkCreateViewHRef={action('/data/create')}
          linkImportViewsHRef={action('/data/import')}
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
          i18nCreateView={text('i18nLinkCreateView', createView)}
          i18nImportViews={importViews}
          i18nImportViewsTip={importViewsTip}
          i18nName={text('i18nName', 'Name')}
          i18nDescription={text('i18nDescription', 'Name')}
          i18nNameFilterPlaceholder={text(
            'i18nNameFilterPlaceholder',
            'Filter by Name...'
          )}
          i18nResultsCount={text(
            'i18nResultsCount',
            viewItems.length + ' Results'
          )}
          children={viewItems}
          hasListData={true}
        />
      </Router>
    ))
  );
