import { IMonitoredIntegration } from "@syndesis/models";
import * as React from 'react';
import './TopIntegrations.css';
export interface ITopIntegrationsProps {
    loading: boolean;
    topIntegrations: IMonitoredIntegration[];
}
export declare class TopIntegrations extends React.Component<ITopIntegrationsProps> {
    render(): JSX.Element;
}
