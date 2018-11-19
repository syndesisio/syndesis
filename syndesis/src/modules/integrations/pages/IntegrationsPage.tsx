import { WithConnections, WithMonitoredIntegrations } from '@syndesis/api';
import { Connection, IntegrationWithOverview } from '@syndesis/models';
import { IntegrationWithMonitoring } from '@syndesis/models/src';
import {
  IActiveFilter,
  IFilterType,
  IListViewToolbarAbstractComponent,
  ISortType,
  IntegrationsList,
  IntegrationsListSkeleton,
  IntegrationsListItem,
  IntegrationsListView,
  ListViewToolbarAbstractComponent,
} from '@syndesis/ui';
import * as React from 'react';

function getFilteredAndSortedIntegrations(
  integrations: IntegrationWithOverview[],
  activeFilters: IActiveFilter[],
  currentSortType: string,
  isSortAscending: boolean
) {
  let filteredAndSortedIntegrations = integrations;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    filteredAndSortedIntegrations = filteredAndSortedIntegrations.filter(
      (mi: IntegrationWithOverview) => {
        if (filter.title === 'Name') {
          return mi.integration.name.toLowerCase().includes(valueToLower);
        }
        if (filter.title === 'Connection') {
          const connectionNames = mi.integration!.flows!.reduce(
            (acc, flow) => [
              ...acc,
              ...flow
                .steps!.filter(s => s.connection)
                .map(s => s.connection!.name.toLowerCase()),
            ],
            [] as string[]
          );
          return connectionNames.reduce(
            (found, n) => found || n.includes(valueToLower),
            false
          );
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
      return left.integration!.currentState!.localeCompare(
        right.integration!.currentState!
      );
    }
  );

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

function getFilterTypes(connections: Connection[]): IFilterType[] {
  return [
    filterByName,
    {
      ...filterByConnection,
      filterValues: connections.map(c => ({
        id: c.id,
        title: c.name,
      })),
    } as IFilterType,
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

export default class IntegrationsPage extends ListViewToolbarAbstractComponent<
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
      <WithMonitoredIntegrations>
        {({ data: integrationsData, loading }) => (
          <WithConnections>
            {({ data: connectionsData }) => {
              const filteredAndSortedIntegrations = getFilteredAndSortedIntegrations(
                integrationsData.items,
                this.state.activeFilters,
                this.state.currentSortType,
                this.state.isSortAscending
              );
              return (
                <IntegrationsListView
                  linkToIntegrationImport={'/integrations/import'}
                  linkToIntegrationCreation={'/integrations/create'}
                  filterTypes={getFilterTypes(connectionsData.items)}
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
                  onToggleCurrentSortDirection={
                    this.onToggleCurrentSortDirection
                  }
                  onUpdateCurrentSortType={this.onUpdateCurrentSortType}
                >
                  {loading ? (
                    <IntegrationsListSkeleton
                      width={800}
                      style={{
                        backgroundColor: '#FFF',
                        marginTop: 30,
                      }}
                    />
                  ) : (
                    <IntegrationsList>
                      {filteredAndSortedIntegrations.map(
                        (mi: IntegrationWithMonitoring, index) => (
                          <IntegrationsListItem
                            integrationId={mi.integration.id!}
                            integrationName={mi.integration.name!}
                            currentState={mi.integration.currentState!}
                            targetState={mi.integration.targetState!}
                            isConfigurationRequired={
                              !!(
                                mi.integration!.board!.warnings ||
                                mi.integration!.board!.errors ||
                                mi.integration!.board!.notices
                              )
                            }
                            monitoringValue={
                              mi.monitoring && mi.monitoring.detailedState.value
                            }
                            monitoringCurrentStep={
                              mi.monitoring &&
                              mi.monitoring.detailedState.currentStep
                            }
                            monitoringTotalSteps={
                              mi.monitoring &&
                              mi.monitoring.detailedState.totalSteps
                            }
                            key={index}
                          />
                        )
                      )}
                    </IntegrationsList>
                  )}
                </IntegrationsListView>
              );
            }}
          </WithConnections>
        )}
      </WithMonitoredIntegrations>
    );
  }
}
