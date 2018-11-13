import * as React from 'react';
export interface IHeader {
    [s: string]: string;
}
export interface IFetch {
    url: string;
    method: 'GET' | 'PUT';
    headers?: IHeader;
    body?: any;
    contentType?: string;
}
export declare function callFetch({ url, method, headers, body, contentType }: IFetch): Promise<Response>;
export interface ISaveProps {
    url: string;
    data: any;
}
export interface IRestState<T> {
    data: T;
    error: boolean;
    errorMessage?: string;
    hasData: boolean;
    loading: boolean;
    read(): Promise<void>;
    save(props: ISaveProps): void;
}
export interface IRestProps<T> {
    autoload?: boolean;
    baseUrl: string;
    poll?: number;
    url: string;
    headers?: IHeader;
    contentType?: string;
    defaultValue: T;
    children(props: IRestState<T>): any;
}
export declare class Rest<T> extends React.Component<IRestProps<T>, IRestState<T>> {
    static defaultProps: {
        autoload: boolean;
    };
    pollingTimer?: number;
    constructor(props: IRestProps<T>);
    componentDidMount(): Promise<void>;
    componentDidUpdate(prevProps: IRestProps<T>): Promise<void>;
    componentWillUnmount(): void;
    shouldComponentUpdate(nextProps: IRestProps<T>, nextState: IRestState<T>): boolean;
    render(): any;
    read(): Promise<void>;
    onSave({ url, data }: ISaveProps): Promise<void>;
    startPolling(): void;
    poller(): void;
    stopPolling(): void;
}
