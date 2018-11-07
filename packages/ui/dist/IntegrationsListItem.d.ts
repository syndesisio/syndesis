import * as React from "react";
export interface IIntegrationsListItemProps {
    integrationId: string;
    integrationName: string;
    currentState: string;
    targetState: string;
    isConfigurationRequired: boolean;
    monitoringValue?: string;
    monitoringCurrentStep?: number;
    monitoringTotalSteps?: number;
}
export declare class IntegrationsListItem extends React.Component<IIntegrationsListItemProps> {
    render(): JSX.Element;
}
