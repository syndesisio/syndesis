import * as React from "react";
export interface IIntegrationBoardProps {
    pendingIntegrations: number;
    runningIntegrations: number;
    stoppedIntegrations: number;
}
export declare class IntegrationBoard extends React.PureComponent<IIntegrationBoardProps> {
    render(): JSX.Element;
}
