import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';

import { ViewInfoList, ViewInfoListItem } from '../../../src';

const stories = storiesOf('Data/Views/ViewInfoList', module);

const viewName1 = 'Customers';
const viewDesc1 = 'Description for Customers';
const viewName2 = 'Accounts';
const viewDesc2 = 'Description for Accounts';
const emptyStateTitle = 'Empty State Title';
const selectionChangedText = 'Selection Changed';

const tableItems = [
  <ViewInfoListItem
    key="viewListItem1"
    name={text('name', viewName1)}
    description={text('description', viewDesc1)}
    connectionName={'connection1'}
    nodePath={[]}
    selected={true}
    i18nUpdate={'Update'}
    isUpdateView={true}
    onSelectionChanged={action(selectionChangedText)}
  />,
  <ViewInfoListItem
    key="viewListItem2"
    name={text('name', viewName2)}
    description={text('description', viewDesc2)}
    connectionName={'connection1'}
    nodePath={[]}
    selected={false}
    i18nUpdate={'Update'}
    isUpdateView={false}
    onSelectionChanged={action(selectionChangedText)}
  />,
];

const hasSourceTablesTestNotes =
  '- Verify toolbar is displayed\n' +
  '- Verify empty state component does not show\n' +
  '- Verify results message shows ' +
  tableItems.length +
  ' results\n' +
  '- Verify ' +
  tableItems.length +
  ' View list items are displayed';

const noSourceTablesTestNotes =
  '- Verify toolbar is displayed\n' +
  '- Verify results message shows 0 results\n' +
  '- Verify no source table items are displayed';

stories

  .add(
    'no Source Tables',
    withNotes(noSourceTablesTestNotes)(() => (
      <Router>
        <ViewInfoList
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
            'There are no currently available Source Tables.'
          )}
          i18nEmptyStateTitle={text('i18nEmptyStateTitle', emptyStateTitle)}
          i18nName={text('i18nName', 'Name')}
          i18nNameFilterPlaceholder={text(
            'i18nNameFilterPlaceholder',
            'Filter by Name...'
          )}
          i18nResultsCount={text('i18nResultsCount', '0 Results')}
          children={[]}
        />
      </Router>
    ))
  )

  .add(
    'has Source Tables',
    withNotes(hasSourceTablesTestNotes)(() => (
      <Router>
        <ViewInfoList
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
            'There are no currently available source tables.'
          )}
          i18nEmptyStateTitle={text('i18nEmptyStateTitle', emptyStateTitle)}
          i18nName={text('i18nName', 'Name')}
          i18nNameFilterPlaceholder={text(
            'i18nNameFilterPlaceholder',
            'Filter by Name...'
          )}
          i18nResultsCount={text(
            'i18nResultsCount',
            tableItems.length + ' Results'
          )}
          children={tableItems}
        />
      </Router>
    ))
  );
