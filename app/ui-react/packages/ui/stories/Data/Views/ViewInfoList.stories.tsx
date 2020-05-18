import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';

import { ViewInfoList, ViewInfoListItems } from '../../../src';

const stories = storiesOf('Data/Views/ViewInfoList', module);

const emptyStateTitle = 'Empty State Title';
const selectionChangedText = 'Selection Changed';

const filteredAndSorted = [
  {
    connectionName: 'pgSample',
    isUpdate: true,
    nodePath: ['sampledb', 'contact'],
    viewName: 'contact',
  },
  {
    connectionName: 'pgSample',
    isUpdate: true,
    nodePath: ['sampledb', 'todo'],
    viewName: 'todo',
  },
  {
    connectionName: 'pgSample',
    isUpdate: true,
    nodePath: ['sampledb', 'winilist'],
    viewName: 'winilist',
  },
];

const tableItems = (
  <ViewInfoListItems
    filteredAndSorted={filteredAndSorted}
    onSelectionChanged={action(selectionChangedText)}
    selectedViewNames={['todo']}
    handleSelectAll={action(selectionChangedText)}
    i18nUpdate={text('i18nUpdate', 'Update')}
    i18nSelectAll={text('i18nUpdate', 'Select all (1 of 3 items selected)')}
  />
);

const hasSourceTablesTestNotes =
  '- Verify toolbar is displayed\n' +
  '- Verify empty state component does not show\n' +
  '- Verify results message shows ' +
  filteredAndSorted.length +
  ' results\n' +
  '- Verify ' +
  filteredAndSorted.length +
  ' View list items are displayed';

const noSourceTablesTestNotes =
  '- Verify toolbar is displayed\n' +
  '- Verify results message shows 0 results\n' +
  '- Verify no source table items are displayed';

stories

  .add(
    'no Source Tables, not loading',
    () => (
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
          connectionLoading={false}
          connectionName={'connName'}
          connectionStatus={<div>DvConnectionStatus Element</div>}
          i18nEmptyStateInfo={text(
            'i18nEmptyStateInfo',
            'There are no currently available Source Tables.'
          )}
          i18nEmptyStateTitle={text('i18nEmptyStateTitle', emptyStateTitle)}
          i18nLastUpdatedMessage={text('i18nLastUpdatedMessage', 'Last updated: xx yy zz')}
          i18nName={text('i18nName', 'Name')}
          i18nNameFilterPlaceholder={text(
            'i18nNameFilterPlaceholder',
            'Filter by Name...'
          )}
          i18nResultsCount={text('i18nResultsCount', '0 Results')}
          i18nRefresh={text('i18nRefresh', 'Refresh')}
          refreshConnectionSchema={action('refreshConnectionSchema')}
          children={[]}
        />
      </Router>
    ),
    { notes: noSourceTablesTestNotes }
  )

  .add(
    'no Source Tables, loading',
    () => (
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
          connectionLoading={true}
          connectionName={'connName'}
          connectionStatus={<div>DvConnectionStatus Element</div>}
          i18nLastUpdatedMessage={text('i18nLastUpdatedMessage', 'Last updated: xx yy zz')}
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
          i18nRefresh={text('i18nRefresh', 'Refresh')}
          refreshConnectionSchema={action('refreshConnectionSchema')}
          children={[]}
        />
      </Router>
    ),
    { notes: noSourceTablesTestNotes }
  )

  .add(
    'has Source Tables, not loading',
    () => (
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
          connectionLoading={false}
          connectionName={'connName'}
          connectionStatus={<div>DvConnectionStatus Element</div>}
          i18nEmptyStateInfo={text(
            'i18nEmptyStateInfo',
            'There are no currently available Source Tables.'
          )}
          i18nEmptyStateTitle={text('i18nEmptyStateTitle', emptyStateTitle)}
          i18nLastUpdatedMessage={text('i18nLastUpdatedMessage', 'Last updated: xx yy zz')}
          i18nName={text('i18nName', 'Name')}
          i18nNameFilterPlaceholder={text(
            'i18nNameFilterPlaceholder',
            'Filter by Name...'
          )}
          i18nResultsCount={text(
            'i18nResultsCount',
            filteredAndSorted.length + ' Results'
          )}
          i18nRefresh={text('i18nRefresh', 'Refresh')}
          refreshConnectionSchema={action('refreshConnectionSchema')}
          children={tableItems}
        />
      </Router>
    ),
    { notes: hasSourceTablesTestNotes }
  )

  .add(
    'has Source Tables, loading',
    () => (
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
          connectionLoading={true}
          connectionName={'connName'}
          connectionStatus={<div>DvConnectionStatus Element</div>}
          i18nEmptyStateInfo={text(
            'i18nEmptyStateInfo',
            'There are no currently available source tables.'
          )}
          i18nEmptyStateTitle={text('i18nEmptyStateTitle', emptyStateTitle)}
          i18nLastUpdatedMessage={text('i18nLastUpdatedMessage', 'Last updated: xx yy zz')}
          i18nName={text('i18nName', 'Name')}
          i18nNameFilterPlaceholder={text(
            'i18nNameFilterPlaceholder',
            'Filter by Name...'
          )}
          i18nResultsCount={text(
            'i18nResultsCount',
            filteredAndSorted.length + ' Results'
          )}
          i18nRefresh={text('i18nRefresh', 'Refresh')}
          refreshConnectionSchema={action('refreshConnectionSchema')}
          children={tableItems}
        />
      </Router>
    ),
    { notes: hasSourceTablesTestNotes }
  );
