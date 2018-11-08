import { IMonitoredIntegration } from "@syndesis/models";
import * as React from 'react';
export interface ITopIntegrationsProps {
    loading: boolean;
    topIntegrations: IMonitoredIntegration[];
}
export declare class TopIntegrations extends React.Component<ITopIntegrationsProps> {
    render(): JSX.Element;
}
