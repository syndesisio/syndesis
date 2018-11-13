import { IntegrationMetricsSummary } from "@syndesis/models";
import * as React from 'react';
import { IRestState } from "./Rest";
export interface IWithIntegrationsMetricsProps {
    children(props: IRestState<IntegrationMetricsSummary>): any;
}
export declare class WithIntegrationsMetrics extends React.Component<IWithIntegrationsMetricsProps> {
    render(): JSX.Element;
}
