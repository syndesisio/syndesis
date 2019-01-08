import { WithConnections, WithMonitoredIntegrations } from '@syndesis/api';
import { Connection, IntegrationWithMonitoring } from '@syndesis/models';
import {
  IActiveFilter,
  IFilterType,
  IntegrationsList,
  IntegrationsListItem,
  IntegrationsListSkeleton,
  IntegrationsListView,
  ISortType,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { NamespacesConsumer } from 'react-i18next';
import { AppContext } from '../../../app';
import i18n from '../../../i18n';
import routes from '../routes';

function getFilteredAndSortedIntegrations(
  integrations: IntegrationWithMonitoring[],
  activeFilters: IActiveFilter[],
  currentSortType: string,
  isSortAscending: boolean
) {
  let filteredAndSortedIntegrations = integrations;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    filteredAndSortedIntegrations = filteredAndSortedIntegrations.filter(
      (mi: IntegrationWithMonitoring) => {
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

export class IntegrationsPage extends React.Component {
  public render() {
    return (
      <WithMonitoredIntegrations>
        {({ data: integrationsData, hasData, error }) => (
          <WithConnections>
            {({ data: connectionsData }) => (
              <AppContext.Consumer>
                {({ config, getPodLogUrl }) => (
                  <NamespacesConsumer ns={['integrations', 'shared']}>
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
                            <IntegrationsListView
                              linkToIntegrationImport={'/integrations/import'}
                              linkToIntegrationCreation={
                                routes.integrations.create.start
                                  .selectConnection
                              }
                              filterTypes={getFilterTypes(
                                connectionsData.items
                              )}
                              sortTypes={sortTypes}
                              resultsCount={
                                filteredAndSortedIntegrations.length
                              }
                              {...helpers}
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
                                      (
                                        mi: IntegrationWithMonitoring,
                                        index
                                      ) => (
                                        <IntegrationsListItem
                                          integrationId={mi.integration.id!}
                                          integrationName={mi.integration.name!}
                                          currentState={
                                            mi.integration.currentState!
                                          }
                                          targetState={
                                            mi.integration.targetState!
                                          }
                                          isConfigurationRequired={
                                            !!(
                                              mi.integration!.board!.warnings ||
                                              mi.integration!.board!.errors ||
                                              mi.integration!.board!.notices
                                            )
                                          }
                                          monitoringValue={
                                            mi.monitoring &&
                                            t(
                                              'integrations:' +
                                                mi.monitoring.detailedState
                                                  .value
                                            )
                                          }
                                          monitoringCurrentStep={
                                            mi.monitoring &&
                                            mi.monitoring.detailedState
                                              .currentStep
                                          }
                                          monitoringTotalSteps={
                                            mi.monitoring &&
                                            mi.monitoring.detailedState
                                              .totalSteps
                                          }
                                          monitoringLogUrl={getPodLogUrl(
                                            config,
                                            mi.monitoring
                                          )}
                                          key={index}
                                          i18nConfigurationRequired={t(
                                            'ConfigurationRequired'
                                          )}
                                          i18nError={t('shared:Error')}
                                          i18nPublished={t('shared:Published')}
                                          i18nUnpublished={t(
                                            'shared:Unpublished'
                                          )}
                                          i18nProgressPending={t(
                                            'shared:Pending'
                                          )}
                                          i18nProgressStarting={t(
                                            'integrations:progressStarting'
                                          )}
                                          i18nProgressStopping={t(
                                            'integrations:progressStopping'
                                          )}
                                          i18nLogUrlText={t('shared:viewLogs')}
                                        />
                                      )
                                    )}
                                  </IntegrationsList>
                                )}
                              </WithLoader>
                            </IntegrationsListView>
                          );
                        }}
                      </WithListViewToolbarHelpers>
                    )}
                  </NamespacesConsumer>
                )}
              </AppContext.Consumer>
            )}
          </WithConnections>
        )}
      </WithMonitoredIntegrations>
    );
  }
}
