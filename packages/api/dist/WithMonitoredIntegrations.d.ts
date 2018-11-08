import { IMonitoredIntegration } from "@syndesis/models";
import * as React from 'react';
import { IRestState } from "./Rest";
export interface IMonitoredIntegrationsResponse {
    items: IMonitoredIntegration[];
    totalCount: number;
}
export interface IWithMonitoredIntegrationsProps {
    children(props: IRestState<IMonitoredIntegrationsResponse>): any;
}
export declare class WithMonitoredIntegrations extends React.Component<IWithMonitoredIntegrationsProps> {
    render(): JSX.Element;
}
