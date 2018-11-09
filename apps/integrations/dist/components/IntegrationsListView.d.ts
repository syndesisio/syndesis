import { IMonitoredIntegration } from "@syndesis/models";
import { IListViewToolbarProps } from "@syndesis/ui";
import * as React from 'react';
export interface IIntegrationsListViewProps extends IListViewToolbarProps {
    match: any;
    loading: boolean;
    monitoredIntegrations: IMonitoredIntegration[];
}
export declare class IntegrationsListView extends React.Component<IIntegrationsListViewProps> {
    render(): JSX.Element;
}
