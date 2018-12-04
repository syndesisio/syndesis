import { WithConnections, WithMonitoredIntegrations } from '@syndesis/api';
import { Connection, IntegrationWithOverview } from '@syndesis/models';
import { IntegrationWithMonitoring } from '@syndesis/models/src';
import {
  IActiveFilter,
  IFilterType,
  IListViewToolbarAbstractComponent,
  IntegrationsList,
  IntegrationsListItem,
  IntegrationsListSkeleton,
  IntegrationsListView,
  ISortType,
  ListViewToolbarAbstractComponent,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { NamespacesConsumer } from 'react-i18next';
import i18n from '../../../i18n';

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
  placeholder: i18n.t('shared:filterByNamePlaceholder'),
  title: i18n.t('shared:Name'),
} as IFilterType;

const filterByConnection = {
  filterType: 'select',
  filterValues: [],
  id: 'connection',
  placeholder: i18n.t('shared:filterByConnectionPlaceholder'),
  title: i18n.t('shared:Connection'),
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
  title: i18n.t('shared:Name'),
} as ISortType;

const sortByStatus = {
  id: 'status',
  isNumeric: false,
  title: i18n.t('shared:Status'),
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
        {({ data: integrationsData, hasData, error }) => (
          <WithConnections>
            {({ data: connectionsData }) => {
              const filteredAndSortedIntegrations = getFilteredAndSortedIntegrations(
                integrationsData.items,
                this.state.activeFilters,
                this.state.currentSortType,
                this.state.isSortAscending
              );
              return (
                <NamespacesConsumer ns={['integrations', 'shared']}>
                  {t => (
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
                      i18nImport={t('shared:Import')}
                      i18nLinkCreateConnection={t(
                        'shared:linkCreateIntegration'
                      )}
                      i18nResultsCount={t('shared:resultsCount', {
                        count: filteredAndSortedIntegrations.length,
                      })}
                    >
                      <WithLoader
                        error={error}
                        loading={!hasData}
                        loaderChildren={
                          <IntegrationsListSkeleton
                            width={800}
                            style={{
                              backgroundColor: '#FFF',
                              marginTop: 30,
                            }}
                          />
                        }
                        errorChildren={<div>TODO</div>}
                      >
                        {() => (
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
                                    mi.monitoring &&
                                    mi.monitoring.detailedState.value
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
                                  i18nConfigurationRequired={t(
                                    'ConfigurationRequired'
                                  )}
                                  i18nPublished={t('shared:Published')}
                                  i18nUnpublished={t('shared:Unpublished')}
                                  i18nProgressStarting={t(
                                    'integrations:progressStarting'
                                  )}
                                  i18nProgressStopping={t(
                                    'integrations:progressStopping'
                                  )}
                                />
                              )
                            )}
                          </IntegrationsList>
                        )}
                      </WithLoader>
                    </IntegrationsListView>
                  )}
                </NamespacesConsumer>
              );
            }}
          </WithConnections>
        )}
      </WithMonitoredIntegrations>
    );
  }
}
