import { IConnection } from "@syndesis/models";
import * as React from 'react';
import { IRestState } from "./Rest";
export interface IConnectionsResponse {
    items: IConnection[];
    totalCount: number;
}
export interface IWithConnectionsProps {
    children(props: IRestState<IConnectionsResponse>): any;
}
export declare class WithConnections extends React.Component<IWithConnectionsProps> {
    render(): JSX.Element;
}
