import * as React from 'react';
export interface IConnectionProps {
    name: string;
    description: string;
    icon: string;
}
export declare class ConnectionCard extends React.PureComponent<IConnectionProps> {
    render(): JSX.Element;
}
