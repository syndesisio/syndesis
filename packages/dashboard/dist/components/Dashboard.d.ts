import { IConnection, IIntegration, IIntegrationsMetrics, IMonitoredIntegration } from "@syndesis/models";
import * as React from 'react';
export interface IIntegrationsPageProps {
    integrationsLoaded: boolean;
    connectionsLoaded: boolean;
    metricsLoaded: boolean;
    integrationsCount: number;
    integrationsErrorCount: number;
    connections: IConnection[];
    connectionsCount: number;
    metrics: IIntegrationsMetrics;
    topIntegrations: IMonitoredIntegration[];
    recentlyUpdatedIntegrations: IIntegration[];
    pendingIntegrations: number;
    runningIntegrations: number;
    stoppedIntegrations: number;
}
export declare class Dashboard extends React.Component<IIntegrationsPageProps> {
    render(): JSX.Element;
}
