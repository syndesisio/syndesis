import * as React from 'react';
import { IRestState } from './Rest';
export interface ISyndesisRestProps<T> {
    autoload?: boolean;
    contentType?: string;
    poll?: number;
    url: string;
    stream?: boolean;
    defaultValue: T;
    children(props: IRestState<T>): any;
}
export declare class SyndesisRest<T> extends React.Component<ISyndesisRestProps<T>> {
    render(): JSX.Element;
}
