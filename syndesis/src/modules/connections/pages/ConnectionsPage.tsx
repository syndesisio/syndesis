import { WithConnections } from '@syndesis/api';
import { Connection } from '@syndesis/models';
import {
  ConnectionCard,
  ConnectionsGrid,
  ConnectionsGridCell,
  ConnectionSkeleton,
  ConnectionsListView,
  IActiveFilter,
  IFilterType,
  IListViewToolbarAbstractComponent,
  ISortType,
  ListViewToolbarAbstractComponent,
} from '@syndesis/ui';
import { getConnectionIcon, WithLoader } from '@syndesis/utils';
import * as React from 'react';

function getFilteredAndSortedConnections(
  connections: Connection[],
  activeFilters: IActiveFilter[],
  currentSortType: string,
  isSortAscending: boolean
) {
  let filteredAndSortedConnections = connections;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    filteredAndSortedConnections = filteredAndSortedConnections.filter(
      (c: Connection) => c.name.toLowerCase().includes(valueToLower)
    );
  });

  filteredAndSortedConnections = filteredAndSortedConnections.sort(
    (miA, miB) => {
      const left = isSortAscending ? miA : miB;
      const right = isSortAscending ? miB : miA;
      return left.name.localeCompare(right.name);
    }
  );

  return filteredAndSortedConnections;
}

const filterByName = {
  filterType: 'text',
  id: 'name',
  placeholder: 'Filter by Name',
  title: 'Name',
} as IFilterType;

const filterTypes = [filterByName];

const sortByName = {
  id: 'name',
  isNumeric: false,
  title: 'Name',
} as ISortType;

const sortTypes: ISortType[] = [sortByName];

export default class ConnectionsPage extends ListViewToolbarAbstractComponent<
  {},
  IListViewToolbarAbstractComponent
> {
  public state = {
    activeFilters: [] as IActiveFilter[],
    currentFilterType: filterByName,
    currentSortType: sortByName.title,
    currentValue: '',
    filterCategory: null,
    isSortAscending: true,
  };

  public render() {
    return (
      <WithConnections>
        {({ data, hasData, error }) => {
          const filteredAndSortedConnections = getFilteredAndSortedConnections(
            data.items,
            this.state.activeFilters,
            this.state.currentSortType,
            this.state.isSortAscending
          );

          return (
            <ConnectionsListView
              linkToConnectionCreate={'/connections/create'}
              filterTypes={filterTypes}
              sortTypes={sortTypes}
              resultsCount={filteredAndSortedConnections.length}
              {...this.state}
              onUpdateCurrentValue={this.onUpdateCurrentValue}
              onValueKeyPress={this.onValueKeyPress}
              onFilterAdded={this.onFilterAdded}
              onSelectFilterType={this.onSelectFilterType}
              onFilterValueSelected={this.onFilterValueSelected}
              onRemoveFilter={this.onRemoveFilter}
              onClearFilters={this.onClearFilters}
              onToggleCurrentSortDirection={this.onToggleCurrentSortDirection}
              onUpdateCurrentSortType={this.onUpdateCurrentSortType}
            >
              <ConnectionsGrid>
                <WithLoader
                  error={error}
                  loading={!hasData}
                  loaderChildren={
                    <>
                      {new Array(5).fill(0).map((_, index) => (
                        <ConnectionsGridCell key={index}>
                          <ConnectionSkeleton />
                        </ConnectionsGridCell>
                      ))}
                    </>
                  }
                  errorChildren={<div>TODO</div>}
                >
                  {() =>
                    data.items.map((c, index) => (
                      <ConnectionsGridCell key={index}>
                        <ConnectionCard
                          name={c.name}
                          description={c.description || ''}
                          icon={getConnectionIcon(c, process.env.PUBLIC_URL)}
                        />
                      </ConnectionsGridCell>
                    ))
                  }
                </WithLoader>
              </ConnectionsGrid>
            </ConnectionsListView>
          );
        }}
      </WithConnections>
    );
  }
}
