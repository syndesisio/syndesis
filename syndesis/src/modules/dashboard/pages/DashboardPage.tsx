import {
  WithConnections,
  WithIntegrationsMetrics,
  WithMonitoredIntegrations,
} from '@syndesis/api';
import {
  IntegrationOverview,
  IntegrationWithMonitoring,
  IntegrationWithOverview,
} from '@syndesis/models';
import {
  AggregatedMetricCard,
  ConnectionCard,
  ConnectionSkeleton,
  ConnectionsMetric,
  Dashboard,
  IntegrationBoard,
  IntegrationsList,
  IntegrationsListItem,
  IntegrationsListSkeleton,
  IntegrationStatus,
  RecentUpdatesCard,
  RecentUpdatesSkeleton,
  TopIntegrationsCard,
  UptimeMetric,
} from '@syndesis/ui';
import { getConnectionIcon, WithLoader } from '@syndesis/utils';
import { Grid } from 'patternfly-react';
import * as React from 'react';

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

export default () => (
  <WithMonitoredIntegrations>
    {({ data: integrationsData, hasData: hasIntegrations }) => (
      <WithIntegrationsMetrics>
        {({ data: metricsData, hasData: hasMetrics }) => (
          <WithConnections>
            {({ data: connectionsData, hasData: hasConnections }) => {
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
                <Dashboard
                  linkToIntegrations={'/integrations'}
                  linkToIntegrationCreation={'/integration/create'}
                  linkToConnections={'/connections'}
                  linkToConnectionCreation={'/connection/create'}
                  integrationsOverview={
                    <AggregatedMetricCard
                      title={`${integrationsData.totalCount} Integrations`}
                      ok={
                        integrationsData.totalCount -
                        integrationStatesCount.Error
                      }
                      error={integrationStatesCount.Error}
                    />
                  }
                  connectionsOverview={
                    <ConnectionsMetric count={connectionsData.totalCount} />
                  }
                  messagesOverview={
                    <AggregatedMetricCard
                      title={`${metricsData.messages} Total Messages`}
                      ok={metricsData.messages! - metricsData.errors!}
                      error={metricsData.errors!}
                    />
                  }
                  uptimeOverview={
                    <UptimeMetric start={parseInt(metricsData.start!, 10)} />
                  }
                  topIntegrations={
                    <TopIntegrationsCard>
                      <WithLoader
                        error={false}
                        loading={!hasIntegrations}
                        loaderChildren={
                          <IntegrationsListSkeleton width={500} />
                        }
                        errorChildren={<div>TODO</div>}
                      >
                        {() => (
                          <IntegrationsList>
                            {topIntegrations.map(
                              (mi: IntegrationWithMonitoring, index) => (
                                <IntegrationsListItem
                                  integrationId={mi.integration.id!}
                                  integrationName={mi.integration.name}
                                  currentState={mi.integration!.currentState!}
                                  targetState={mi.integration!.targetState!}
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
                                />
                              )
                            )}
                          </IntegrationsList>
                        )}
                      </WithLoader>
                    </TopIntegrationsCard>
                  }
                  integrationBoard={
                    <IntegrationBoard
                      runningIntegrations={integrationStatesCount.Published}
                      pendingIntegrations={integrationStatesCount.Pending}
                      stoppedIntegrations={integrationStatesCount.Unpublished}
                    />
                  }
                  integrationUpdates={
                    <RecentUpdatesCard>
                      <WithLoader
                        error={false}
                        loading={!hasIntegrations}
                        loaderChildren={<RecentUpdatesSkeleton />}
                        errorChildren={<div>TODO</div>}
                      >
                        {() =>
                          recentlyUpdatedIntegrations.map(i => (
                            <Grid.Row key={i.id}>
                              <Grid.Col sm={5}>{i.name}</Grid.Col>
                              <Grid.Col sm={3}>
                                <IntegrationStatus
                                  currentState={i.currentState}
                                />
                              </Grid.Col>
                              <Grid.Col sm={4}>
                                {new Date(
                                  i.updatedAt! || i.createdAt!
                                ).toLocaleString()}
                              </Grid.Col>
                            </Grid.Row>
                          ))
                        }
                      </WithLoader>
                    </RecentUpdatesCard>
                  }
                  connections={
                    <WithLoader
                      error={false}
                      loading={!hasConnections}
                      loaderChildren={
                        <>
                          {new Array(5).fill(0).map((_, index) => (
                            <ConnectionSkeleton key={index} />
                          ))}
                        </>
                      }
                      errorChildren={<div>TODO</div>}
                    >
                      {() =>
                        connectionsData.items.map((c, index) => (
                          <ConnectionCard
                            name={c.name}
                            description={c.description || ''}
                            icon={getConnectionIcon(c, process.env.PUBLIC_URL)}
                            key={index}
                          />
                        ))
                      }
                    </WithLoader>
                  }
                />
              );
            }}
          </WithConnections>
        )}
      </WithIntegrationsMetrics>
    )}
  </WithMonitoredIntegrations>
);
