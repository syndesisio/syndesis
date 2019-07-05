import { render } from 'react-testing-library';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { ConnectionsListView } from '../../src/Connection';

it('renders the heading', () => {
  const noop = () => false;
  const { container } = render(
    <Router>
      <ConnectionsListView
        linkToConnectionCreate={'/create'}
        activeFilters={[]}
        filterTypes={[]}
        currentFilterType={{
          filterType: 'text',
          id: 'foo',
          placeholder: 'foo placeholder',
          title: 'Foo',
        }}
        currentSortType={{
          id: 'sort',
          isNumeric: false,
          title: 'Sort',
        }}
        currentValue={''}
        isSortAscending={true}
        resultsCount={0}
        sortTypes={[]}
        onUpdateCurrentValue={noop}
        onValueKeyPress={noop}
        onFilterAdded={noop}
        onSelectFilterType={noop}
        onFilterValueSelected={noop}
        onRemoveFilter={noop}
        onClearFilters={noop}
        onToggleCurrentSortDirection={noop}
        onUpdateCurrentSortType={noop}
        i18nLinkCreateConnection="Create Connection"
        i18nResultsCount="0 Results"
      />
    </Router>
  );
  expect(container).toBeTruthy();
});
