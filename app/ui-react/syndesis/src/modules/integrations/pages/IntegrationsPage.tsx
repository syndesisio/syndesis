import { WithConnections, WithMonitoredIntegrations } from '@syndesis/api';
import { Connection, IntegrationWithMonitoring } from '@syndesis/models';
import {
  IActiveFilter,
  IFilterType,
  IntegrationsListView,
  ISortType,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import i18n from '../../../i18n';
import { PageTitle } from '../../../shared';
import { Integrations } from '../components';
import resolvers from '../resolvers';

function getFilteredAndSortedIntegrations(
  integrations: IntegrationWithMonitoring[],
  activeFilters: IActiveFilter[],
  currentSortType: ISortType,
  isSortAscending: boolean
) {
  let filteredAndSortedIntegrations = integrations;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    filteredAndSortedIntegrations = filteredAndSortedIntegrations.filter(
      (mi: IntegrationWithMonitoring) => {
        if (filter.id === 'name') {
          return mi.integration.name.toLowerCase().includes(valueToLower);
        }
        if (filter.id === 'connection') {
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
            false as boolean
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
      if (currentSortType.id === 'name') {
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
  placeholder: i18n.t('filterByConnectionPlaceholder'),
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

export class IntegrationsPage extends React.Component {
  public render() {
    return (
      <WithConnections debounceWait={500}>
        {({ data: connectionsData }) => (
          <WithMonitoredIntegrations>
            {({
              data: integrationsData,
              hasData,
              error,
              errorMessage,
              loading,
            }) => (
              <Translation ns={['integrations', 'shared']}>
                {t => (
                  <WithListViewToolbarHelpers
                    defaultFilterType={filterByName}
                    defaultSortType={sortByName}
                  >
                    {helpers => {
                      const filteredAndSortedIntegrations = getFilteredAndSortedIntegrations(
                        integrationsData.items,
                        helpers.activeFilters,
                        helpers.currentSortType,
                        helpers.isSortAscending
                      );

                      return (
                        <>
                          <PageTitle title={t('shared:Integrations')} />
                          <IntegrationsListView
                            linkToIntegrationImport={resolvers.import()}
                            linkToManageCiCd={resolvers.manageCicd.root()}
                            linkToIntegrationCreation={resolvers.create.start.selectStep()}
                            filterTypes={getFilterTypes(
                              connectionsData.connectionsForDisplay
                            )}
                            sortTypes={sortTypes}
                            resultsCount={filteredAndSortedIntegrations.length}
                            {...helpers}
                            i18nTitle={t('shared:Integrations')}
                            i18nDescription={t('integrationListDescription')}
                            i18nImport={t('shared:Import')}
                            i18nManageCiCd={t('ManageCiCd')}
                            i18nLinkCreateIntegration={t(
                              'shared:linkCreateIntegration'
                            )}
                            i18nLinkCreateIntegrationTip={t(
                              'integrationsEmptyState.createTip'
                            )}
                            i18nResultsCount={t('shared:resultsCount', {
                              count: filteredAndSortedIntegrations.length,
                            })}
                          >
                            <Integrations
                              error={error}
                              errorMessage={errorMessage}
                              loading={loading && !hasData}
                              integrations={filteredAndSortedIntegrations}
                            />
                          </IntegrationsListView>
                        </>
                      );
                    }}
                  </WithListViewToolbarHelpers>
                )}
              </Translation>
            )}
          </WithMonitoredIntegrations>
        )}
      </WithConnections>
    );
  }
}
