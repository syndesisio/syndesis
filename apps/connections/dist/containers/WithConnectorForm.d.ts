import * as React from 'react';
export interface IWithConnectorState {
    ConnectorForm?: any;
    loading: boolean;
    error: boolean;
}
export interface IWithConnectorForm {
    connectorId: string;
    children(props: IWithConnectorState): any;
}
export declare class WithConnectorForm extends React.Component<IWithConnectorForm, IWithConnectorState> {
    state: {
        loading: boolean;
        error: boolean;
    };
    componentDidMount(): Promise<void>;
    render(): any;
}
