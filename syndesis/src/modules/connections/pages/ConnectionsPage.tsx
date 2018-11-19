import { WithConnections } from '@syndesis/api';
import { Connection } from '@syndesis/models';
import {
  IActiveFilter,
  IFilterType,
  IListViewToolbarAbstractComponent,
  ISortType,
  ListViewToolbarAbstractComponent,
} from '@syndesis/ui';
import * as React from 'react';
import { ConnectionsListView } from '../components/ConnectionsListView';
import { ConnectionsAppContext } from '../ConnectionsAppContext';

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
      <ConnectionsAppContext.Consumer>
        {({ baseurl }) => (
          <WithConnections>
            {({ data, loading, hasData }) => {
              const filteredAndSortedConnections = getFilteredAndSortedConnections(
                data.items,
                this.state.activeFilters,
                this.state.currentSortType,
                this.state.isSortAscending
              );
              return (
                <ConnectionsListView
                  loading={!hasData && loading}
                  baseurl={baseurl}
                  connections={filteredAndSortedConnections}
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
                  onToggleCurrentSortDirection={
                    this.onToggleCurrentSortDirection
                  }
                  onUpdateCurrentSortType={this.onUpdateCurrentSortType}
                />
              );
            }}
          </WithConnections>
        )}
      </ConnectionsAppContext.Consumer>
    );
  }
}
