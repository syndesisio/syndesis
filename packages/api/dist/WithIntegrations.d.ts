import { IIntegration } from "@syndesis/models";
import * as React from 'react';
import { IRestState } from "./Rest";
export interface IIntegrationsResponse {
    items: IIntegration[];
    totalCount: number;
}
export interface IWithIntegrationsProps {
    children(props: IRestState<IIntegrationsResponse>): any;
}
export declare class WithIntegrations extends React.Component<IWithIntegrationsProps> {
    render(): JSX.Element;
}
