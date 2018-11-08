import { WithConnections, WithIntegrationsMetrics, WithMonitoredIntegrations } from "@syndesis/api";
import * as React from 'react';
import { Dashboard } from "../components";
export function getIntegrationsCountsByState(integrations) {
    return integrations.reduce((counts, mi) => {
        const stateCount = counts[mi.integration.currentState] || 0;
        return {
            ...counts,
            [mi.integration.currentState]: stateCount + 1
        };
    }, {
        Error: 0,
        Pending: 0,
        Published: 0,
        Unpublished: 0
    });
}
export function getTimestamp(integration) {
    return integration.updatedAt !== 0
        ? integration.updatedAt
        : integration.createdAt;
}
export function byTimestamp(a, b) {
    const aTimestamp = getTimestamp(a);
    const bTimestamp = getTimestamp(b);
    return bTimestamp - aTimestamp;
}
export function getRecentlyUpdatedIntegrations(integrations) {
    return integrations
        .map(mi => mi.integration)
        .sort(byTimestamp)
        .slice(0, 5);
}
export function getTopIntegrations(integrations, topIntegrations = {}) {
    const topIntegrationsArray = Object.keys(topIntegrations)
        .map(key => {
        return {
            count: topIntegrations[key],
            id: key
        };
    })
        .sort((a, b) => {
        return b.count - a.count;
    });
    return integrations
        .sort((miA, miB) => byTimestamp(miA.integration, miB.integration))
        .sort((a, b) => {
        const index = topIntegrationsArray.findIndex(i => i.id === b.integration.id);
        return index === -1 ? topIntegrationsArray.length + 1 : index;
    })
        .reverse()
        .slice(0, 5);
}
export default () => (React.createElement(WithMonitoredIntegrations, null, ({ data: integrationsData, hasData: hasIntegrations }) => (React.createElement(WithIntegrationsMetrics, null, ({ data: metricsData, hasData: hasMetrics }) => (React.createElement(WithConnections, null, ({ data: connectionsData, hasData: hasConnections }) => {
    const integrationStatesCount = getIntegrationsCountsByState(integrationsData.items);
    return (React.createElement(Dashboard, { integrationsLoaded: hasIntegrations, connectionsLoaded: hasConnections, metricsLoaded: hasMetrics, integrationsCount: integrationsData.totalCount, integrationsErrorCount: integrationStatesCount.Error, connections: connectionsData.items, connectionsCount: connectionsData.totalCount, metrics: metricsData, runningIntegrations: integrationStatesCount.Published, stoppedIntegrations: integrationStatesCount.Unpublished, pendingIntegrations: integrationStatesCount.Pending, recentlyUpdatedIntegrations: getRecentlyUpdatedIntegrations(integrationsData.items), topIntegrations: getTopIntegrations(integrationsData.items, metricsData.topIntegrations) }));
}))))));
//# sourceMappingURL=DashboardPage.js.map