import * as React from 'react';
import { RouteComponentProps } from 'react-router';
export interface IWithRouterProps extends RouteComponentProps {
    children(router: RouteComponentProps): any;
}
export declare class WithRouterBase extends React.Component<IWithRouterProps> {
    render(): any;
}
export declare const WithRouter: React.ComponentClass<Pick<IWithRouterProps, "children">, any>;
