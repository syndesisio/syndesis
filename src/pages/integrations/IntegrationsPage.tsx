import * as React from 'react';
import { IntegrationsListView } from '../../components';
import { IActiveFilter, IFilterType, ISortType } from '../../components/ListViewToolbar';
import {
  IConnection,
  IMonitoredIntegration,
  WithConnections,
  WithMonitoredIntegrations,
  WithRouter
} from '../../containers';
import {
  IListViewToolbarAbstractComponent,
  ListViewToolbarAbstractComponent
} from '../../containers/ListViewToolbarAbstractComponent';

function getFilteredAndSortedIntegrations(integrations: IMonitoredIntegration[], activeFilters: IActiveFilter[], currentSortType: string, isSortAscending: boolean) {
  let filteredAndSortedIntegrations = integrations;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    filteredAndSortedIntegrations = filteredAndSortedIntegrations.filter(
      (mi: IMonitoredIntegration) => {
        if (filter.title === 'Name') {
          return mi.integration.name.toLowerCase().includes(valueToLower);
        }
        if (filter.title === 'Connection') {
          const connectionNames = mi.integration.flows
            .reduce((acc, flow) =>
                [
                  ...acc,
                  ...flow.steps
                    .filter(s => s.connection)
                    .map(s => s.connection.name.toLowerCase())
                ],
              []
            );
          return connectionNames.reduce((found, n) => found || n.includes(valueToLower), false);
        }
        return false;
      }
    );
  });

  filteredAndSortedIntegrations = filteredAndSortedIntegrations.sort(
    (miA, miB) => {
      const left = isSortAscending ? miA : miB;
      const right = isSortAscending ? miB : miA;
      if (currentSortType === 'Name') {
        return left.integration.name.localeCompare(right.integration.name);
      }
      return left.integration.currentState.localeCompare(right.integration.currentState);
    });

  return filteredAndSortedIntegrations;
}

const filterByName = {
  filterType: 'text',
  id: 'name',
  placeholder: 'Filter by Name',
  title: 'Name',
} as IFilterType;

const filterByConnection = {
  filterType: 'select',
  filterValues: [],
  id: 'connection',
  placeholder: 'Filter by Connection',
  title: 'Connection',
};

function getFilterTypes(connections: IConnection[]): IFilterType[] {
  return [
    filterByName,
    {
      ...filterByConnection,
      filterValues: connections.map(c => ({
        id: c.id,
        title: c.name,
      }))
    } as IFilterType
  ];
}

const sortByName = {
  id: 'name',
  isNumeric: false,
  title: 'Name',
} as ISortType;

const sortByStatus = {
  id: 'status',
  isNumeric: false,
  title: 'Status',
} as ISortType;

const sortTypes: ISortType[] = [sortByName, sortByStatus];

export class IntegrationsPage extends ListViewToolbarAbstractComponent<{}, IListViewToolbarAbstractComponent> {
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
      <WithMonitoredIntegrations>
        {({integrationsCount, integrations}) =>
          <WithConnections>
            {({connections}) =>
              <WithRouter>
                {({match}) => {
                  const filteredAndSortedIntegrations = getFilteredAndSortedIntegrations(
                    integrations,
                    this.state.activeFilters,
                    this.state.currentSortType,
                    this.state.isSortAscending
                  );
                  return (
                    <IntegrationsListView
                      match={match}
                      monitoredIntegrations={filteredAndSortedIntegrations}
                      filterTypes={getFilterTypes(connections)}
                      sortTypes={sortTypes}
                      resultsCount={filteredAndSortedIntegrations.length}
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
        }
      </WithMonitoredIntegrations>
    );
  }
}