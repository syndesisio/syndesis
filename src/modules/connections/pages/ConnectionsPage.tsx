import * as React from 'react';
import { ConnectionsListView } from '../../../components/index';
import { IActiveFilter, IFilterType, ISortType } from '../../../components/ListViewToolbar';
import { IConnection, WithConnections, WithRouter } from '../../../containers/index';
import {
  IListViewToolbarAbstractComponent,
  ListViewToolbarAbstractComponent
} from '../../../containers/ListViewToolbarAbstractComponent';

function getFilteredAndSortedConnections(connections: IConnection[], activeFilters: IActiveFilter[], currentSortType: string, isSortAscending: boolean) {
  let filteredAndSortedConnections = connections;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    filteredAndSortedConnections = filteredAndSortedConnections.filter(
      (c: IConnection) => c.name.toLowerCase().includes(valueToLower)
    );
  });

  filteredAndSortedConnections = filteredAndSortedConnections.sort(
    (miA, miB) => {
      const left = isSortAscending ? miA : miB;
      const right = isSortAscending ? miB : miA;
      return left.name.localeCompare(right.name);
    });

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

export default class ConnectionsPage extends ListViewToolbarAbstractComponent<{}, IListViewToolbarAbstractComponent> {
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
        {({data, loading}) =>
          <WithRouter>
            {({match}) => {
              const filteredAndSortedConnections = getFilteredAndSortedConnections(
                data.items,
                this.state.activeFilters,
                this.state.currentSortType,
                this.state.isSortAscending
              );
              return (
                <ConnectionsListView
                  loading={loading}
                  match={match}
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
                  onToggleCurrentSortDirection={this.onToggleCurrentSortDirection}
                  onUpdateCurrentSortType={this.onUpdateCurrentSortType}
                />
              );
            }}
          </WithRouter>
        }
      </WithConnections>
    );
  }
}