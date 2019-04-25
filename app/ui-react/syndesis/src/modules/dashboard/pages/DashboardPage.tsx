import {
  WithConnections,
  WithIntegrationsMetrics,
  WithMonitoredIntegrations,
} from '@syndesis/api';
import {
  Connection,
  IntegrationOverview,
  IntegrationWithOverview,
} from '@syndesis/models';
import {
  AggregatedMetricCard,
  ConnectionsMetric,
  Dashboard,
  IntegrationBoard,
  RecentUpdatesCard,
  RecentUpdatesItem,
  RecentUpdatesSkeleton,
  TopIntegrationsCard,
  UptimeMetric,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { ApiError } from '../../../shared';
import { Connections } from '../../connections/shared';
import { Integrations } from '../../integrations/components';
import resolvers from '../../resolvers';

export interface IIntegrationCountsByState {
  Error: number;
  Pending: number;
  Published: number;
  Unpublished: number;
}

export function getIntegrationsCountsByState(
  integrations: IntegrationWithOverview[]
): IIntegrationCountsByState {
  return integrations.reduce(
    (counts, mi) => {
      const stateCount = counts[mi.integration.currentState!] || 0;
      return {
        ...counts,
        [mi.integration.currentState!]: stateCount + 1,
      };
    },
    {
      Error: 0,
      Pending: 0,
      Published: 0,
      Unpublished: 0,
    } as IIntegrationCountsByState
  );
}

export function getTimestamp(integration: IntegrationOverview) {
  return integration.updatedAt !== 0
    ? integration.updatedAt
    : integration.createdAt;
}

export function byTimestamp(a: IntegrationOverview, b: IntegrationOverview) {
  const aTimestamp = getTimestamp(a) || 0;
  const bTimestamp = getTimestamp(b) || 0;
  return bTimestamp - aTimestamp;
}

export function getRecentlyUpdatedIntegrations(
  integrations: IntegrationWithOverview[]
): IntegrationOverview[] {
  return integrations
    .map(mi => mi.integration)
    .sort(byTimestamp)
    .slice(0, 5);
}

export function getTopIntegrations(
  integrations: IntegrationWithOverview[],
  topIntegrations: { [name: string]: number } = {}
): IntegrationWithOverview[] {
  const topIntegrationsArray = Object.keys(topIntegrations)
    .map(key => {
      return {
        count: topIntegrations[key],
        id: key,
      } as any;
    })
    .sort((a, b) => {
      return b.count - a.count;
    });

  return integrations
    .sort((miA, miB) => byTimestamp(miA.integration, miB.integration))
    .sort((a, b) => {
      const index = topIntegrationsArray.findIndex(
        i => i.id === b.integration.id
      );
      return index === -1 ? topIntegrationsArray.length + 1 : index;
    })
    .reverse()
    .slice(0, 5);
}

export function getConnectionHref(connection: Connection) {
  return resolvers.connections.connection.details({ connection });
}

export default () => (
  <WithMonitoredIntegrations>
    {({
      data: integrationsData,
      hasData: hasIntegrations,
      error: integrationsError,
    }) => (
      <WithIntegrationsMetrics>
        {({ data: metricsData }) => (
          <WithConnections>
            {({
              data: connectionsData,
              hasData: hasConnections,
              error: connectionsError,
            }) => {
              const integrationStatesCount = getIntegrationsCountsByState(
                integrationsData.items
              );
              const recentlyUpdatedIntegrations = getRecentlyUpdatedIntegrations(
                integrationsData.items
              );
              const topIntegrations = getTopIntegrations(
                integrationsData.items,
                metricsData.topIntegrations
              );
              return (
                <Translation ns={['dashboard', 'integrations', 'shared']}>
                  {t => (
                    <Dashboard
                      linkToIntegrations={resolvers.integrations.list()}
                      linkToIntegrationCreation={resolvers.integrations.create.start.selectStep()}
                      linkToConnections={resolvers.connections.connections()}
                      linkToConnectionCreation={resolvers.connections.create.selectConnector()}
                      integrationsOverview={
                        <div data-testid="total-integrations">
                          <AggregatedMetricCard
                            title={t('titleTotalIntegrations', {
                              count: integrationsData.totalCount,
                            })}
                            ok={
                              integrationsData.totalCount -
                              integrationStatesCount.Error
                            }
                            error={integrationStatesCount.Error}
                          />
                        </div>
                      }
                      connectionsOverview={
                        <div data-testid="total-connections">
                          <ConnectionsMetric
                            i18nTitle={t('titleTotalConnections', {
                              count:
                                connectionsData.connectionsForDisplay.length,
                            })}
                          />
                        </div>
                      }
                      messagesOverview={
                        <div data-testid="total-messages">
                          <AggregatedMetricCard
                            title={t('titleTotalMessages', {
                              count: metricsData.messages,
                            })}
                            ok={metricsData.messages! - metricsData.errors!}
                            error={metricsData.errors!}
                          />
                        </div>
                      }
                      uptimeOverview={
                        <UptimeMetric
                          start={parseInt(metricsData.start!, 10)}
                          i18nTitle={t('titleUptimeMetric')}
                        />
                      }
                      topIntegrations={
                        <TopIntegrationsCard
                          i18nTitle={t('titleTopIntegrations', {
                            count: 5,
                          })}
                          i18nLast30Days={t('lastNumberOfDays', {
                            numberOfDays: 30,
                          })}
                          i18nLast60Days={t('lastNumberOfDays', {
                            numberOfDays: 60,
                          })}
                          i18nLast90Days={t('lastNumberOfDays', {
                            numberOfDays: 90,
                          })}
                        >
                          <Integrations
                            error={integrationsError}
                            loading={!hasIntegrations}
                            integrations={topIntegrations}
                          />
                        </TopIntegrationsCard>
                      }
                      integrationBoard={
                        <IntegrationBoard
                          runningIntegrations={integrationStatesCount.Published}
                          pendingIntegrations={integrationStatesCount.Pending}
                          stoppedIntegrations={
                            integrationStatesCount.Unpublished
                          }
                          i18nTitle={t('titleIntegrationBoard')}
                          i18nIntegrationStatePending={t(
                            'integrationStatePending'
                          )}
                          i18nIntegrationStateRunning={t(
                            'integrationStateRunning'
                          )}
                          i18nIntegrationStateStopped={t(
                            'integrationStateStopped'
                          )}
                          i18nIntegrations={t('shared:Integrations')}
                          i18nTotal={t('shared:Total')}
                        />
                      }
                      integrationUpdates={
                        <RecentUpdatesCard
                          i18nTitle={t('titleIntegrationUpdates')}
                        >
                          <WithLoader
                            error={false}
                            loading={!hasIntegrations}
                            loaderChildren={<RecentUpdatesSkeleton />}
                            errorChildren={<ApiError />}
                          >
                            {() =>
                              recentlyUpdatedIntegrations.map(i => (
                                <RecentUpdatesItem
                                  key={i.id}
                                  integrationName={i.name}
                                  integrationCurrentState={i.currentState!}
                                  integrationDate={i.updatedAt! || i.createdAt!}
                                  i18nError={t('shared:Error')}
                                  i18nPublished={t('shared:Published')}
                                  i18nUnpublished={t('shared:Unpublished')}
                                />
                              ))
                            }
                          </WithLoader>
                        </RecentUpdatesCard>
                      }
                      connections={
                        <Connections
                          error={connectionsError}
                          includeConnectionMenu={false}
                          loading={!hasConnections}
                          connections={connectionsData.connectionsForDisplay}
                          getConnectionHref={getConnectionHref}
                        />
                      }
                      i18nConnections={t('shared:Connections')}
                      i18nLinkCreateConnection={t(
                        'shared:linkCreateConnection'
                      )}
                      i18nLinkCreateIntegration={t(
                        'shared:linkCreateIntegration'
                      )}
                      i18nLinkToConnections={t('linkToConnections')}
                      i18nLinkToIntegrations={t('linkToIntegrations')}
                      i18nTitle={t('title')}
                    />
                  )}
                </Translation>
              );
            }}
          </WithConnections>
        )}
      </WithIntegrationsMetrics>
    )}
  </WithMonitoredIntegrations>
);
