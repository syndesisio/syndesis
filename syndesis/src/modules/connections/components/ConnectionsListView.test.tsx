import { shallow } from 'enzyme';
import * as React from 'react';

import { ConnectionsListView } from './ConnectionsListView';

it('renders the heading', () => {
  const noop = () => false;
  const result = shallow(
    <ConnectionsListView
      baseurl={'/'}
      loading={true}
      connections={[]}
      activeFilters={[]}
      filterTypes={[]}
      currentFilterType={{
        filterType: 'text',
        id: 'foo',
        placeholder: 'foo placeholder',
        title: 'Foo',
      }}
      currentSortType={'sort'}
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
    />
  );
  expect(result).toBeTruthy();
});
