import * as React from 'react';
import { shallow } from 'enzyme';

import { ConnectionsListView } from './ConnectionsListView';

it('renders the heading', () => {
  const result = shallow(
    <ConnectionsListView
      baseurl={'/'}
      loading={true}
      connections={[]}
      activeFilters={[]}
      filterTypes={[]}
      currentFilterType={{
        id: 'foo',
        filterType: 'text',
        placeholder: 'foo placeholder',
        title: 'Foo',
      }}
      currentSortType={'sort'}
      currentValue={''}
      isSortAscending={true}
      resultsCount={0}
      sortTypes={[]}
      onUpdateCurrentValue={() => false}
      onValueKeyPress={() => false}
      onFilterAdded={() => false}
      onSelectFilterType={() => false}
      onFilterValueSelected={() => false}
      onRemoveFilter={() => false}
      onClearFilters={() => false}
      onToggleCurrentSortDirection={() => false}
      onUpdateCurrentSortType={() => false}
    />
  );
  expect(result).toBeTruthy();
});
