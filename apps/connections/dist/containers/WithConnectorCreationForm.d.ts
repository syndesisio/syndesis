import * as React from 'react';
export interface IConnectorModule {
    CreationForm: any;
}
export declare function loadModule(connectorId: string): Promise<IConnectorModule>;
export interface IWithConnectorState {
    CreationForm?: any;
    loading: boolean;
    error: boolean;
}
export interface IWithConnectorForm {
    connectorId: string;
    children(props: IWithConnectorState): any;
}
export declare class WithConnectorCreationForm extends React.Component<IWithConnectorForm, IWithConnectorState> {
    state: {
        loading: boolean;
        error: boolean;
    };
    componentDidMount(): Promise<void>;
    render(): any;
}
